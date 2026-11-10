/*
 * Interface do Portal de Recursos.
 * Sem framework: chama a API REST (/api/v1) com a sessão do navegador e o token CSRF (cookie XSRF-TOKEN).
 * Todo texto vindo da API é escapado antes de ir para o HTML (sem XSS) e nada usa script inline (CSP).
 */
(function () {
  'use strict';

  const estado = {
    usuario: null, selecionados: new Set(), vistos: new Map(), temporizador: null,
    feriados: new Map(), vigiaAvisos: null, vigiaRecursos: null,
    buscaSeq: 0, situacoes: new Map(), editandoRecurso: null
  };
  const $ = (seletor) => document.querySelector(seletor);

  // ------------------------------------------------------------------ utilidades

  function esc(valor) {
    return String(valor ?? '').replace(/[&<>"']/g, (c) => ({
      '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    })[c]);
  }

  function cookie(nome) {
    const par = document.cookie.split('; ').find((c) => c.startsWith(nome + '='));
    return par ? decodeURIComponent(par.substring(nome.length + 1)) : null;
  }

  function lerLocal(chave) {
    try { return window.localStorage.getItem(chave); } catch (e) { return null; }
  }

  function gravarLocal(chave, valor) {
    try { window.localStorage.setItem(chave, valor); } catch (e) { /* sem armazenamento: só perde a marcação */ }
  }

  const MESES = ['jan', 'fev', 'mar', 'abr', 'mai', 'jun', 'jul', 'ago', 'set', 'out', 'nov', 'dez'];

  function dataHora(iso) {
    if (!iso) return '';
    const [data, hora] = iso.split('T');
    const [a, m, d] = data.split('-');
    return `${d}/${m}/${a} ${hora.substring(0, 5)}`;
  }

  function paraApi(valorInput) {
    if (!valorInput) return null;
    return valorInput.length === 16 ? valorInput + ':00' : valorInput;
  }

  function icone(nome) {
    return `<svg class="ic"><use href="#i-${nome}"/></svg>`;
  }

  class ErroApi extends Error {
    constructor(status, corpo) {
      super((corpo && corpo.detail) || 'Não foi possível concluir a operação.');
      this.status = status;
      this.corpo = corpo || {};
    }
  }

  async function api(metodo, url, corpo) {
    const cabecalhos = { Accept: 'application/json' };
    if (corpo !== undefined) cabecalhos['Content-Type'] = 'application/json';
    const token = cookie('XSRF-TOKEN');
    if (token && metodo !== 'GET') cabecalhos['X-XSRF-TOKEN'] = token;
    const resposta = await fetch(url, {
      method: metodo,
      headers: cabecalhos,
      body: corpo === undefined ? undefined : JSON.stringify(corpo),
      credentials: 'same-origin'
    });
    const dados = resposta.status === 204 ? null : await resposta.json().catch(() => null);
    if (!resposta.ok) throw new ErroApi(resposta.status, dados);
    return dados;
  }

  // ------------------------------------------------------------------ avisos

  function mostrar(tipo, titulo, detalhe, itens) {
    const caixa = $('#mensagem');
    const lista = (itens || []).map((i) => `<li>${esc(i)}</li>`).join('');
    caixa.className = `toast-sor alert-${tipo}`;
    caixa.setAttribute('role', tipo === 'danger' ? 'alert' : 'status');
    caixa.innerHTML = `<div class="toast-corpo"><strong>${esc(titulo)}</strong> ${esc(detalhe || '')}${lista ? `<ul>${lista}</ul>` : ''}</div>
      <button type="button" class="toast-fechar" aria-label="Fechar aviso" title="Fechar">${icone('fechar')}</button>`;
    clearTimeout(estado.temporizador);
    estado.temporizador = setTimeout(() => { caixa.className = 'd-none'; }, 9000);
  }

  $('#mensagem').addEventListener('click', (evento) => {
    if (!evento.target.closest('.toast-fechar')) return;
    clearTimeout(estado.temporizador);
    $('#mensagem').className = 'd-none';
  });

  // ------------------------------------------------------------------ janela central (modal)

  const ICONES_MODAL = { sucesso: 'check', erro: 'alerta', aviso: 'alerta', pergunta: 'alerta' };
  let fecharModalAtual = null;

  /**
   * Abre a janela central e devolve uma Promise com o valor do botão escolhido (ou null se fechada).
   * botoes: [{ texto, valor, classe }]; o último é o botão principal e recebe o foco.
   */
  function abrirModal({ tipo, titulo, texto, itens, botoes }) {
    if (fecharModalAtual) fecharModalAtual(null);
    const modal = $('#modal');
    const opcoes = botoes || [{ texto: 'Entendi', valor: true, classe: 'btn-primary' }];
    modal.dataset.tipo = tipo;
    $('#modal-icone').innerHTML = icone(ICONES_MODAL[tipo] || 'alerta');
    $('#modal-titulo').textContent = titulo;
    $('#modal-texto').innerHTML = `<p>${esc(texto || '')}</p>${(itens || []).length
      ? `<ul>${itens.map((i) => `<li>${esc(i)}</li>`).join('')}</ul>` : ''}`;
    $('#modal-acoes').innerHTML = opcoes.map((b, i) =>
      `<button type="button" class="btn ${b.classe || 'btn-suave'}" data-modal-valor="${i}" data-testid="modal-botao-${i}">${esc(b.texto)}</button>`).join('');
    const anterior = document.activeElement;
    modal.classList.remove('d-none');
    document.body.classList.add('com-modal');
    const principal = $('#modal-acoes').lastElementChild;
    if (principal) principal.focus();
    return new Promise((resolver) => {
      fecharModalAtual = (valor) => {
        modal.classList.add('d-none');
        document.body.classList.remove('com-modal');
        fecharModalAtual = null;
        if (anterior && anterior.focus) anterior.focus();
        resolver(valor);
      };
      modal.onclick = (evento) => {
        const botao = evento.target.closest('[data-modal-valor]');
        if (botao) fecharModalAtual(opcoes[Number(botao.dataset.modalValor)].valor);
        else if (evento.target.closest('[data-modal-fechar]') || evento.target === modal) fecharModalAtual(null);
      };
    });
  }

  document.addEventListener('keydown', (evento) => {
    if (evento.key === 'Escape' && fecharModalAtual) fecharModalAtual(null);
  });

  const MOTIVOS_CONFLITO = { RESERVA: 'já reservado', MANUTENCAO: 'em manutenção', RECURSO_INATIVO: 'desativado',
    CONCORRENCIA: 'reservado por outra pessoa agora há pouco' };

  /** Erro de API em forma de janela central (usado nas ações de reserva e cancelamento). */
  function falhaEmModal(erro) {
    if (!(erro instanceof ErroApi) || erro.status === 401) {
      falha(erro);
      return Promise.resolve(null);
    }
    const corpo = erro.corpo;
    const itens = (corpo.conflitos || []).map((c) => `${c.recursoCodigo || 'Recurso'}: ${MOTIVOS_CONFLITO[c.motivo] || c.motivo}`
      + (c.inicio && c.fim ? ` (${dataHora(c.inicio).substring(11)} – ${dataHora(c.fim).substring(11)})` : ''));
    (corpo.erros || []).forEach((e) => itens.push(`${e.campo}: ${e.mensagem}`));
    const indisponivel = erro.status === 409;
    return abrirModal({
      tipo: indisponivel ? 'aviso' : 'erro',
      titulo: corpo.title || (indisponivel ? 'Indisponível' : 'Não foi possível concluir'),
      texto: erro.message,
      itens
    });
  }

  function sucesso(texto) {
    mostrar('success', 'Pronto.', texto);
  }

  function falha(erro) {
    if (!(erro instanceof ErroApi)) {
      mostrar('danger', 'Falha de comunicação.', 'Verifique sua conexão e tente novamente.');
      return;
    }
    if (erro.status === 401) {
      telaLogin();
      mostrar('warning', 'Sessão encerrada.', 'Entre novamente para continuar.');
      return;
    }
    const corpo = erro.corpo;
    const itens = [];
    (corpo.erros || []).forEach((e) => itens.push(`${e.campo}: ${e.mensagem}`));
    if (corpo.rastreio) itens.push(`Código de rastreio: ${corpo.rastreio}`);
    mostrar('danger', (corpo.title || 'Erro') + '.', erro.message, itens);
  }

  async function executar(acao) {
    try {
      await acao();
    } catch (erro) {
      falha(erro);
    }
  }

  const vazio = (texto) =>
    `<div class="vazio">${icone('vazio').replace('class="ic"', 'class="ic ic-xl"')}<p>${texto}</p></div>`;

  // ------------------------------------------------------------------ sessão

  /** Ao sair nada da sessão anterior pode ficar na tela (filtros, escolhas, finalidade, justificativa). */
  function limparTela() {
    document.querySelectorAll('#tela-app form').forEach((f) => f.reset());
    estado.selecionados.clear();
    estado.situacoes.clear();
    estado.editandoRecurso = null;
    $('#grade-recursos').innerHTML = '';
    atualizarPainel();
  }

  function telaLogin() {
    estado.usuario = null;
    clearInterval(estado.vigiaAvisos);
    clearInterval(estado.vigiaRecursos);
    limparTela();
    $('#tela-app').classList.add('d-none');
    $('#tela-login').classList.remove('d-none');
  }

  const PERFIS = { SOLICITANTE: 'Solicitante', RESPONSAVEL: 'Responsável', ADMINISTRADOR: 'Administrador' };

  function telaApp(usuario) {
    estado.usuario = usuario;
    $('#usuario-atual').textContent = usuario.nome;
    $('#perfil-atual').textContent = PERFIS[usuario.perfil] || usuario.perfil;
    $('#avatar').textContent = usuario.nome.trim().charAt(0).toUpperCase();
    $('#saudacao').textContent = `Olá, ${usuario.nome.trim().split(/\s+/)[0]}`;
    $('#tela-login').classList.add('d-none');
    $('#tela-app').classList.remove('d-none');
    document.querySelectorAll('[data-perfis]').forEach((el) => {
      const permitido = el.dataset.perfis.split(',').includes(usuario.perfil);
      (el.closest('li') || el).classList.toggle('d-none', !permitido);
    });
    sugerirPeriodo();
    abrirAba('reservar');
    executar(verificarAvisos);
    clearInterval(estado.vigiaAvisos);
    estado.vigiaAvisos = setInterval(() => { verificarAvisos().catch(() => { /* tenta de novo no próximo ciclo */ }); }, 30000);
    clearInterval(estado.vigiaRecursos);
    estado.vigiaRecursos = setInterval(atualizarReservarEmSilencio, 20000);
  }

  async function iniciar() {
    try {
      telaApp(await api('GET', '/api/v1/auth/me'));
    } catch (erro) {
      telaLogin();
    }
  }

  $('#form-login').addEventListener('submit', (evento) => {
    evento.preventDefault();
    executar(async () => {
      const resposta = await fetch('/api/v1/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded', Accept: 'application/json' },
        body: new URLSearchParams({ username: $('#login-usuario').value, password: $('#login-senha').value }),
        credentials: 'same-origin'
      });
      const corpo = await resposta.json().catch(() => null);
      if (!resposta.ok) {
        $('#login-erro').textContent = (corpo && corpo.detail) || 'Usuário ou senha inválidos.';
        $('#login-erro').classList.remove('d-none');
        $('#login-senha').value = '';
        $('#login-senha').focus();
        return;
      }
      $('#login-erro').classList.add('d-none');
      $('#mensagem').className = 'd-none';
      $('#form-login').reset();
      telaApp(await api('GET', '/api/v1/auth/me'));
    });
  });

  ['#login-usuario', '#login-senha'].forEach((s) => $(s).addEventListener('input', () => $('#login-erro').classList.add('d-none')));

  $('#mostrar-senha').addEventListener('click', () => {
    const campo = $('#login-senha');
    const mostrar = campo.type === 'password';
    campo.type = mostrar ? 'text' : 'password';
    const botao = $('#mostrar-senha');
    botao.setAttribute('aria-pressed', String(mostrar));
    botao.setAttribute('aria-label', mostrar ? 'Ocultar senha' : 'Mostrar senha');
    botao.title = mostrar ? 'Ocultar senha' : 'Mostrar senha';
    botao.innerHTML = icone(mostrar ? 'olho-fechado' : 'olho');
    campo.focus();
  });

  $('#botao-sair').addEventListener('click', () => executar(async () => {
    await api('POST', '/api/v1/auth/logout');
    telaLogin();
    sucesso('Sessão encerrada.');
  }));

  // ------------------------------------------------------------------ abas

  const TITULOS = {
    reservar: 'Reservar', minhas: 'Minhas reservas', aprovacoes: 'Aprovações e uso',
    movimentacoes: 'Retiradas e devoluções', admin: 'Administração', notificacoes: 'Avisos'
  };

  const carregadores = {
    reservar: () => pesquisar(),
    minhas: carregarMinhas,
    aprovacoes: carregarAprovacoes,
    movimentacoes: carregarMovimentacoes,
    admin: carregarAdmin,
    notificacoes: carregarNotificacoes
  };

  function abrirAba(nome) {
    document.querySelectorAll('#abas .menu-item').forEach((b) => {
      const ativa = b.dataset.aba === nome;
      b.classList.toggle('ativo', ativa);
      if (ativa) b.setAttribute('aria-current', 'page'); else b.removeAttribute('aria-current');
    });
    document.querySelectorAll('.aba').forEach((s) => s.classList.toggle('d-none', s.id !== `aba-${nome}`));
    $('#titulo-pagina').textContent = TITULOS[nome];
    executar(carregadores[nome]);
  }

  $('#abas').addEventListener('click', (evento) => {
    const botao = evento.target.closest('[data-aba]');
    if (botao) abrirAba(botao.dataset.aba);
  });

  const nomesEstado = {
    SOLICITADA: 'Aguardando aprovação', APROVADA: 'Aprovada', EM_USO: 'Em uso', CONCLUIDA: 'Concluída',
    REJEITADA: 'Rejeitada', CANCELADA: 'Cancelada', NAO_COMPARECEU: 'Não compareceu'
  };

  const selo = (e) => `<span class="selo selo-${esc(e)}" data-estado="${esc(e)}">${esc(nomesEstado[e] || e)}</span>`;
  const horario = (r) => `${dataHora(r.inicio).substring(11)} – ${dataHora(r.fim).substring(11)}`;
  const listaRecursos = (r) => r.recursos.map((x) => esc(x.codigo)).join(', ');

  /** Cartão de reserva com a "folha de calendário" (mês em âmbar). */
  function cartaoReserva(r, extras) {
    const [, mes, dia] = r.inicio.split('T')[0].split('-');
    return `<div class="reserva-card" data-reserva="${r.id}">
      <div class="rv-data"><small>${MESES[Number(mes) - 1]}</small><b>${Number(dia)}</b></div>
      <div class="rv-corpo">
        <h3>${esc(r.finalidade)}</h3>
        <div class="rv-linha">
          <span>${icone('relogio')}${esc(horario(r))}</span>
          <span>${icone('sala')}${listaRecursos(r)}</span>
          <span>${icone('professor')}${esc(r.solicitante.nome)}</span>
          <span>Nº ${r.id}</span>
        </div>
      </div>
      <div class="rv-acoes">${selo(r.estado)}${extras.acoes || ''}</div>
      ${extras.decisao || ''}
    </div>`;
  }

  // ------------------------------------------------------------------ reservar

  const ROTULO_TIPO = { SALA: 'Sala', PROFESSOR: 'Professor', MATERIAL: 'Material', EQUIPAMENTO: 'Equipamento' };

  function doisDigitos(n) { return String(n).padStart(2, '0'); }

  function amanha(hora) {
    const d = new Date();
    d.setDate(d.getDate() + 1);
    return `${d.getFullYear()}-${doisDigitos(d.getMonth() + 1)}-${doisDigitos(d.getDate())}T${doisDigitos(hora)}:00`;
  }

  function agoraLocal() {
    const d = new Date();
    return `${d.getFullYear()}-${doisDigitos(d.getMonth() + 1)}-${doisDigitos(d.getDate())}T${doisDigitos(d.getHours())}:${doisDigitos(d.getMinutes())}`;
  }

  /** O seletor nativo não oferece datas passadas; o servidor continua sendo quem decide (RN de período futuro). */
  function limitarDatas() {
    const minimo = agoraLocal();
    ['#busca-inicio', '#busca-fim', '#manutencao-inicio', '#manutencao-fim'].forEach((s) => { $(s).min = minimo; });
  }

  function sugerirPeriodo() {
    limitarDatas();
    if (!$('#busca-inicio').value) {
      $('#busca-inicio').value = amanha(10);
      $('#busca-fim').value = amanha(12);
    }
    resumirPeriodo();
  }

  async function feriadosDe(ano) {
    if (!estado.feriados.has(ano)) {
      estado.feriados.set(ano, new Set(await api('GET', `/api/v1/disponibilidade/feriados?ano=${ano}`)));
    }
    return estado.feriados.get(ano);
  }

  /** Dias (aaaa-mm-dd) tocados pelo período; término à meia-noite não conta o dia seguinte. */
  function diasDoPeriodo(inicio, fim) {
    const dias = [];
    const cursor = new Date(inicio.substring(0, 10) + 'T00:00:00');
    let ultimo = fim.substring(0, 10);
    if (fim.substring(11, 16) === '00:00') {
      const d = new Date(ultimo + 'T00:00:00');
      d.setDate(d.getDate() - 1);
      ultimo = `${d.getFullYear()}-${doisDigitos(d.getMonth() + 1)}-${doisDigitos(d.getDate())}`;
    }
    for (let i = 0; i < 31; i += 1) {
      const dia = `${cursor.getFullYear()}-${doisDigitos(cursor.getMonth() + 1)}-${doisDigitos(cursor.getDate())}`;
      if (dia > ultimo) break;
      dias.push(dia);
      cursor.setDate(cursor.getDate() + 1);
    }
    return dias;
  }

  /** Avisa sobre data passada ou feriado antes do envio. Devolve o problema que impede a reserva, se houver. */
  async function validarPeriodo() {
    const aviso = $('#aviso-periodo');
    const inicio = $('#busca-inicio').value;
    const fim = $('#busca-fim').value;
    let problema = null;
    if (inicio && fim) {
      if (inicio < agoraLocal()) {
        problema = 'Esse horário já passou. Escolha uma data e hora futura.';
      } else {
        const dias = diasDoPeriodo(inicio, fim);
        for (const dia of dias) {
          const feriados = await feriadosDe(Number(dia.substring(0, 4)));
          if (feriados.has(dia)) {
            problema = `${dia.substring(8, 10)}/${dia.substring(5, 7)}/${dia.substring(0, 4)} é feriado; não há reservas nesse dia.`;
            break;
          }
        }
      }
    }
    aviso.textContent = problema || '';
    aviso.classList.toggle('d-none', !problema);
    return problema;
  }

  $('#aba-reservar').addEventListener('click', (evento) => {
    const botao = evento.target.closest('[data-preset]');
    if (!botao) return;
    const [de, ate] = botao.dataset.preset === 'manha' ? [8, 12] : [14, 18];
    $('#busca-inicio').value = amanha(de);
    $('#busca-fim').value = amanha(ate);
    resumirPeriodo();
    executar(validarPeriodo);
    executar(pesquisar);
  });

  function resumirPeriodo() {
    const inicio = paraApi($('#busca-inicio').value);
    const fim = paraApi($('#busca-fim').value);
    $('#resumo-periodo').textContent = inicio && fim
      ? `${dataHora(inicio).substring(0, 10)} · ${dataHora(inicio).substring(11)} às ${dataHora(fim).substring(11)}`
      : 'Informe o início e o término na busca.';
  }
  ['#busca-inicio', '#busca-fim'].forEach((s) => $(s).addEventListener('change', () => {
    resumirPeriodo();
    executar(validarPeriodo);
    executar(pesquisar);
  }));

  $('#form-busca').addEventListener('submit', (evento) => {
    evento.preventDefault();
    executar(pesquisar);
  });

  function cartaoRecurso(r) {
    const meta = [];
    if (r.capacidade) meta.push(['Capacidade', `${r.capacidade} lugares`]);
    if (r.localizacao) meta.push(['Local', esc(r.localizacao)]);
    if (r.descricao) meta.push(['Detalhe', esc(r.descricao)]);
    const tags = r.competencias.map((c) => `<span class="selo sem-ponto">${esc(c)}</span>`).join('')
      + (r.restrito ? '<span class="selo selo-aviso sem-ponto">Exige aprovação</span>' : '');
    const marcado = estado.selecionados.has(r.id);
    const situacao = estado.situacoes.get(r.id);
    const bloqueado = situacao && !situacao.disponivel;
    return `<label class="recurso-card tipo-${esc(r.tipo)}${marcado ? ' escolhido' : ''}${bloqueado ? ' indisponivel' : ''}" data-recurso="${r.id}"${bloqueado ? ' aria-disabled="true"' : ''}>
      <span class="rc-topo"><span class="rc-tipo">${esc(ROTULO_TIPO[r.tipo] || r.tipo)}</span>
        <input type="checkbox" class="form-check-input escolher" value="${r.id}" aria-label="Escolher ${esc(r.codigo)}"${marcado ? ' checked' : ''}${bloqueado ? ' disabled' : ''}></span>
      <strong class="rc-nome">${esc(r.nome)}</strong>
      <span class="rc-codigo">${esc(r.codigo)}</span>
      <span class="rc-meta">${meta.map(([k, v]) => `<span><b>${k}</b>${v}</span>`).join('')}</span>
      <span class="rc-tags">${tags}</span>
      <span class="situacao">${situacao ? textoSituacao(situacao) : ''}</span>
    </label>`;
  }

  async function pesquisar() {
    const filtros = new URLSearchParams();
    const campos = {
      tipo: '#busca-tipo', texto: '#busca-texto', capacidadeMinima: '#busca-capacidade',
      localizacao: '#busca-localizacao', competencia: '#busca-competencia'
    };
    Object.entries(campos).forEach(([chave, seletor]) => { if ($(seletor).value) filtros.set(chave, $(seletor).value); });
    const inicio = paraApi($('#busca-inicio').value);
    const fim = paraApi($('#busca-fim').value);
    if ($('#busca-livres').checked && inicio && fim) {
      filtros.set('disponivelInicio', inicio);
      filtros.set('disponivelFim', fim);
    }
    const seq = ++estado.buscaSeq;
    const recursos = await api('GET', '/api/v1/recursos?' + filtros);
    if (seq !== estado.buscaSeq) return; // uma busca mais nova já respondeu
    recursos.forEach((r) => estado.vistos.set(r.id, r));
    const ids = new Set(recursos.map((r) => r.id));
    [...estado.selecionados].forEach((id) => { if (!ids.has(id)) estado.selecionados.delete(id); });
    estado.situacoes = await consultarSituacoes([...ids]);
    if (seq !== estado.buscaSeq) return;
    [...estado.selecionados].forEach((id) => {
      const s = estado.situacoes.get(id);
      if (s && !s.disponivel) estado.selecionados.delete(id);
    });
    $('#grade-recursos').innerHTML = recursos.length === 0
      ? vazio('Nenhum recurso encontrado com esses filtros.')
      : SECOES.map((secao) => secaoRecursos(secao, recursos.filter((r) => r.tipo === secao.tipo))).join('');
    atualizarPainel();
  }

  /** Situação de todos os recursos listados no período escolhido (livre, reservado, em manutenção). */
  async function consultarSituacoes(ids) {
    const inicio = paraApi($('#busca-inicio').value);
    const fim = paraApi($('#busca-fim').value);
    if (ids.length === 0 || !inicio || !fim || fim <= inicio) return new Map();
    const lista = await api('GET', `/api/v1/disponibilidade?recursoIds=${ids.join(',')}&inicio=${inicio}&fim=${fim}`);
    return new Map(lista.map((s) => [s.recursoId, s]));
  }

  function textoSituacao(s) {
    if (s.disponivel) return '<span class="livre">Livre neste período</span>';
    const emManutencao = s.motivos.some((m) => m.motivo === 'MANUTENCAO');
    const rotulo = s.motivos.some((m) => m.motivo === 'RECURSO_INATIVO') ? 'Desativado'
      : emManutencao ? 'Em manutenção' : 'Reservado neste horário';
    return `<span class="ocupado">${rotulo}</span><small>${s.motivos.map((m) => esc(motivo(m))).join('<br>')}</small>`;
  }

  /** Atualização periódica da aba Reservar: novos cadastros e horários liberados aparecem sozinhos. */
  function atualizarReservarEmSilencio() {
    const visivel = !$('#aba-reservar').classList.contains('d-none') && document.visibilityState === 'visible';
    if (!estado.usuario || !visivel || fecharModalAtual) return;
    pesquisar().catch(() => { /* tenta de novo no próximo ciclo */ });
  }

  const SECOES = [
    { tipo: 'SALA', titulo: 'Salas', icone: 'sala' },
    { tipo: 'PROFESSOR', titulo: 'Professores', icone: 'professor' },
    { tipo: 'EQUIPAMENTO', titulo: 'Equipamentos', icone: 'equipamento' },
    { tipo: 'MATERIAL', titulo: 'Materiais', icone: 'material' }
  ];

  function secaoRecursos(secao, itens) {
    if (itens.length === 0) return '';
    return `<section class="secao-recursos" aria-label="${secao.titulo}">
      <h2 class="secao-recursos-titulo">${icone(secao.icone)}${secao.titulo}<span class="contagem">${itens.length}</span></h2>
      <div class="grade-itens">${itens.map(cartaoRecurso).join('')}</div>
    </section>`;
  }

  /** RN-032: até 1 sala, 1 professor e 3 materiais/equipamentos por reserva (o servidor também valida). */
  const LIMITES = [
    { tipos: ['SALA'], maximo: 1, texto: 'uma sala' },
    { tipos: ['PROFESSOR'], maximo: 1, texto: 'um professor' },
    { tipos: ['MATERIAL', 'EQUIPAMENTO'], maximo: 3, texto: 'três materiais ou equipamentos' }
  ];

  function limiteExcedido(novoId) {
    const novo = estado.vistos.get(novoId);
    if (!novo) return null;
    const limite = LIMITES.find((l) => l.tipos.includes(novo.tipo));
    const jaEscolhidos = [...estado.selecionados]
      .map((id) => estado.vistos.get(id))
      .filter((r) => r && limite.tipos.includes(r.tipo)).length;
    return jaEscolhidos >= limite.maximo ? limite : null;
  }

  $('#grade-recursos').addEventListener('change', (evento) => {
    if (!evento.target.classList.contains('escolher') || evento.target.disabled) return;
    const id = Number(evento.target.value);
    const excedido = evento.target.checked ? limiteExcedido(id) : null;
    if (excedido) {
      evento.target.checked = false;
      mostrar('warning', 'Limite por reserva.', `Cada reserva pode ter no máximo ${excedido.texto}. Desmarque outro ou faça uma reserva separada.`);
      return;
    }
    if (evento.target.checked) estado.selecionados.add(id); else estado.selecionados.delete(id);
    evento.target.closest('.recurso-card').classList.toggle('escolhido', evento.target.checked);
    atualizarPainel();
  });

  function atualizarPainel() {
    const chips = [...estado.selecionados].map((id) => {
      const r = estado.vistos.get(id);
      return `<span class="chip-escolhido">${esc(r ? r.codigo : '#' + id)}</span>`;
    });
    $('#escolhidos').innerHTML = chips.length ? chips.join('') : '<span class="texto-suave">Nenhum recurso escolhido.</span>';
  }

  $('#botao-disponibilidade').addEventListener('click', () => executar(pesquisar));

  function motivo(m) {
    if (m.motivo === 'RECURSO_INATIVO') return 'Recurso desativado pela administração';
    return `${dataHora(m.inicio)} – ${dataHora(m.fim).substring(11)}`;
  }

  $('#form-reserva').addEventListener('submit', (evento) => {
    evento.preventDefault();
    executar(async () => {
      const problema = await validarPeriodo();
      if (problema) {
        await abrirModal({ tipo: 'aviso', titulo: 'Período indisponível', texto: problema });
        return;
      }
      if (estado.selecionados.size === 0) {
        await abrirModal({ tipo: 'aviso', titulo: 'Escolha um recurso', texto: 'Marque ao menos um recurso livre para reservar.' });
        return;
      }
      let reserva;
      try {
        reserva = await api('POST', '/api/v1/reservas', {
          inicio: paraApi($('#busca-inicio').value), fim: paraApi($('#busca-fim').value),
          finalidade: $('#reserva-finalidade').value,
          justificativa: $('#reserva-justificativa').value || null,
          recursoIds: [...estado.selecionados]
        });
      } catch (erro) {
        await falhaEmModal(erro);
        pesquisar().catch(() => { /* a grade se atualiza no próximo ciclo */ });
        return;
      }
      $('#form-reserva').reset();
      estado.selecionados.clear();
      await pesquisar();
      verificarAvisos().catch(() => { /* o aviso chega no próximo ciclo */ });
      const aprovada = reserva.estado === 'APROVADA';
      await abrirModal({
        tipo: 'sucesso',
        titulo: aprovada ? 'Reserva confirmada' : 'Solicitação enviada',
        texto: aprovada
          ? `A reserva nº ${reserva.id} foi confirmada para ${dataHora(reserva.inicio)} – ${dataHora(reserva.fim).substring(11)}.`
          : `A reserva nº ${reserva.id} foi registrada e aguarda a aprovação do responsável.`,
        itens: reserva.recursos.map((r) => `${r.codigo} — ${r.nome}`)
      });
    });
  });

  // ------------------------------------------------------------------ minhas reservas

  async function carregarMinhas() {
    const reservas = await api('GET', '/api/v1/reservas/minhas');
    $('#lista-minhas').innerHTML = reservas.length === 0
      ? vazio('Você ainda não tem reservas. Escolha um recurso em <strong>Reservar</strong>.')
      : reservas.map((r) => cartaoReserva(r, {
        acoes:
          (['SOLICITADA', 'APROVADA'].includes(r.estado)
            ? `<button class="btn btn-perigo btn-pequeno" data-acao="cancelar" data-id="${r.id}">Cancelar</button>` : '')
          + `<button class="btn btn-suave btn-pequeno" data-acao="historico" data-id="${r.id}">Histórico</button>`
      })).join('');
  }

  $('#lista-minhas').addEventListener('click', (evento) => {
    const botao = evento.target.closest('button[data-acao]');
    if (!botao) return;
    const id = botao.dataset.id;
    executar(async () => {
      if (botao.dataset.acao === 'cancelar') {
        const confirmar = await abrirModal({
          tipo: 'pergunta',
          titulo: `Cancelar a reserva nº ${id}?`,
          texto: 'O horário fica livre para outras pessoas. Essa ação não pode ser desfeita.',
          botoes: [
            { texto: 'Manter reserva', valor: false, classe: 'btn-suave' },
            { texto: 'Cancelar reserva', valor: true, classe: 'btn-perigo-cheio' }
          ]
        });
        if (!confirmar) return;
        try {
          await api('POST', `/api/v1/reservas/${id}/cancelar`, { motivo: 'Cancelada pelo solicitante' });
        } catch (erro) {
          await falhaEmModal(erro);
          return;
        }
        await carregarMinhas();
        await abrirModal({ tipo: 'sucesso', titulo: 'Reserva cancelada', texto: `A reserva nº ${id} foi cancelada e o horário foi liberado.` });
      } else {
        await alternarHistorico(botao, id);
      }
    });
  });

  const ACOES_HISTORICO = {
    RESERVA_CRIADA: 'Reserva criada', RESERVA_APROVADA: 'Aprovada', RESERVA_REJEITADA: 'Rejeitada',
    RESERVA_CANCELADA: 'Cancelada', RESERVA_ALTERADA: 'Alterada', RESERVA_EM_USO: 'Uso iniciado',
    RESERVA_CONCLUIDA: 'Concluída', RESERVA_NAO_COMPARECEU: 'Não compareceu'
  };

  /** Cada cartão abre e fecha o próprio histórico, logo abaixo dele. */
  async function alternarHistorico(botao, id) {
    const cartao = botao.closest('.reserva-card');
    const aberto = cartao.querySelector('.rv-historico');
    if (aberto) {
      aberto.remove();
      botao.textContent = 'Histórico';
      botao.setAttribute('aria-expanded', 'false');
      return;
    }
    const eventos = await api('GET', `/api/v1/reservas/${id}/historico`);
    cartao.insertAdjacentHTML('beforeend', `<div class="rv-historico" aria-label="Histórico da reserva ${esc(id)}">
      <ol class="linha-tempo">${eventos.map((e) => `<li>
        <span class="lt-quando">${esc(dataHora(e.ocorridoEm))}</span>
        <span class="lt-o-que"><strong>${esc(ACOES_HISTORICO[e.acao] || e.acao)}</strong> por ${esc(e.ator)}${e.estadoNovo
          ? ` <span class="texto-suave">· ${esc(nomesEstado[e.estadoAnterior] || e.estadoAnterior || 'início')} → ${esc(nomesEstado[e.estadoNovo] || e.estadoNovo)}</span>` : ''}${e.detalhes
          ? `<br><small class="texto-suave">${esc(e.detalhes)}</small>` : ''}</span></li>`).join('')}</ol></div>`);
    botao.textContent = 'Ocultar histórico';
    botao.setAttribute('aria-expanded', 'true');
  }

  // ------------------------------------------------------------------ aprovações e uso

  const DECISAO = `<div class="rv-decisao">
      <input class="form-control motivo" placeholder="Motivo da decisão (obrigatório para rejeitar)" aria-label="Motivo da decisão">
      <button class="btn btn-ok btn-pequeno" data-acao="aprovar">Aprovar</button>
      <button class="btn btn-perigo btn-pequeno" data-acao="rejeitar">Rejeitar</button></div>`;

  async function carregarAprovacoes() {
    const [fila, aprovadas, emUso] = await Promise.all([
      api('GET', '/api/v1/reservas?estado=SOLICITADA'),
      api('GET', '/api/v1/reservas?estado=APROVADA'),
      api('GET', '/api/v1/reservas?estado=EM_USO')
    ]);
    $('#lista-fila').innerHTML = fila.length === 0
      ? vazio('Nenhuma solicitação aguardando decisão.')
      : fila.map((r) => cartaoReserva(r, {
        decisao: DECISAO.replace('data-acao="aprovar"', `data-acao="aprovar" data-id="${r.id}"`)
          .replace('data-acao="rejeitar"', `data-acao="rejeitar" data-id="${r.id}"`)
      })).join('');
    const uso = aprovadas.concat(emUso);
    $('#lista-uso').innerHTML = uso.length === 0
      ? vazio('Nenhuma reserva aprovada ou em uso.')
      : uso.map((r) => cartaoReserva(r, {
        acoes: r.estado === 'APROVADA'
          ? `<button class="btn btn-suave btn-pequeno" data-acao="iniciar" data-id="${r.id}">Iniciar uso</button>
             <button class="btn btn-perigo btn-pequeno" data-acao="nao-comparecimento" data-id="${r.id}">Não compareceu</button>`
          : `<button class="btn btn-suave btn-pequeno" data-acao="concluir" data-id="${r.id}">Concluir</button>`
      })).join('');
  }

  $('#aba-aprovacoes').addEventListener('click', (evento) => {
    const botao = evento.target.closest('button[data-acao]');
    if (!botao) return;
    const id = botao.dataset.id;
    const campo = botao.closest('.reserva-card').querySelector('.motivo');
    executar(async () => {
      const reserva = await api('POST', `/api/v1/reservas/${id}/${botao.dataset.acao}`,
        campo ? { motivo: campo.value || null } : undefined);
      sucesso(`Reserva #${id}: ${nomesEstado[reserva.estado]}.`);
      await carregarAprovacoes();
    });
  });

  // ------------------------------------------------------------------ retiradas

  async function carregarMovimentacoes() {
    const abertas = await api('GET', '/api/v1/movimentacoes?situacao=ABERTAS');
    $('#lista-movimentacoes').innerHTML = abertas.length === 0
      ? vazio('Nenhum material pendente de devolução.')
      : abertas.map((m) => `<div class="reserva-card" data-mov="${m.id}">
          <div class="rv-data"><small>res.</small><b>${m.reservaId}</b></div>
          <div class="rv-corpo"><h3>${esc(m.recursoNome)} <span class="texto-suave">· ${esc(m.recursoCodigo)}</span></h3>
            <div class="rv-linha"><span>${icone('relogio')}Devolver até ${esc(dataHora(m.prazoDevolucao))}</span></div></div>
          <div class="rv-acoes">${m.atrasada ? '<span class="selo selo-perigo">Atrasada</span>' : '<span class="selo selo-ok">No prazo</span>'}
            <label class="check-suave"><input type="checkbox" class="form-check-input avaria"> avaria</label>
            <button class="btn btn-primary btn-pequeno" data-devolver="${m.id}">Registrar devolução</button></div></div>`).join('');
  }

  $('#retirada-reserva').addEventListener('change', () => executar(async () => {
    const seletor = $('#retirada-recurso');
    const numero = $('#retirada-reserva').value;
    if (!numero) {
      seletor.innerHTML = '<option value="">Informe a reserva primeiro</option>';
      return;
    }
    const reserva = await api('GET', `/api/v1/reservas/${numero}`);
    const itens = reserva.recursos.filter((r) => ['MATERIAL', 'EQUIPAMENTO'].includes(r.tipo));
    seletor.innerHTML = itens.length === 0
      ? '<option value="">Esta reserva não tem materiais</option>'
      : itens.map((r) => `<option value="${r.id}">${esc(r.codigo)} — ${esc(r.nome)}</option>`).join('');
  }));

  $('#form-retirada').addEventListener('submit', (evento) => {
    evento.preventDefault();
    executar(async () => {
      await api('POST', '/api/v1/movimentacoes/retiradas', {
        reservaId: Number($('#retirada-reserva').value), recursoId: Number($('#retirada-recurso').value)
      });
      sucesso('Retirada registrada.');
      evento.target.reset();
      $('#retirada-recurso').innerHTML = '<option value="">Informe a reserva primeiro</option>';
      await carregarMovimentacoes();
    });
  });

  $('#lista-movimentacoes').addEventListener('click', (evento) => {
    const botao = evento.target.closest('button[data-devolver]');
    if (!botao) return;
    const avaria = botao.closest('.reserva-card').querySelector('.avaria').checked;
    executar(async () => {
      await api('POST', `/api/v1/movimentacoes/${botao.dataset.devolver}/devolucao`, { avaria });
      sucesso('Devolução registrada.');
      await carregarMovimentacoes();
    });
  });

  // ------------------------------------------------------------------ administração

  const carregadoresAdmin = {
    visao: carregarVisaoGeral,
    recursos: carregarRecursosAdmin,
    manutencao: carregarManutencoes,
    relatorios: () => Promise.resolve()
  };

  function abrirSubAba(nome) {
    document.querySelectorAll('#sub-abas-admin .sub-aba-botao').forEach((b) => {
      const ativa = b.dataset.sub === nome;
      b.classList.toggle('ativo', ativa);
      if (ativa) b.setAttribute('aria-current', 'page'); else b.removeAttribute('aria-current');
    });
    document.querySelectorAll('#aba-admin .sub-aba').forEach((p) => p.classList.toggle('d-none', p.dataset.painel !== nome));
    return carregadoresAdmin[nome]();
  }

  $('#sub-abas-admin').addEventListener('click', (evento) => {
    const botao = evento.target.closest('[data-sub]');
    if (botao) executar(() => abrirSubAba(botao.dataset.sub));
  });

  function carregarAdmin() {
    const ativa = document.querySelector('#sub-abas-admin .sub-aba-botao.ativo');
    if (!$('#relatorio-inicio').value) {
      const hoje = new Date();
      $('#relatorio-inicio').value = `${hoje.getFullYear()}-${doisDigitos(hoje.getMonth() + 1)}-01T00:00`;
      $('#relatorio-fim').value = `${agoraLocal().substring(0, 10)}T23:59`;
    }
    return abrirSubAba(ativa ? ativa.dataset.sub : 'visao');
  }

  async function carregarVisaoGeral() {
    const [resumo, auditoria] = await Promise.all([
      api('GET', '/api/v1/relatorios/resumo'),
      api('GET', '/api/v1/auditoria')
    ]);
    const indicadores = [
      ['Aguardando aprovação', resumo.solicitacoesPendentes, resumo.solicitacoesPendentes > 0],
      ['Aprovadas', resumo.reservasPorEstado.APROVADA], ['Em uso', resumo.reservasPorEstado.EM_USO],
      ['Materiais pendentes', resumo.movimentacoesAbertas], ['Devoluções atrasadas', resumo.movimentacoesAtrasadas],
      ['Manutenções ativas', resumo.manutencoesAtivas], ['Conflitos evitados', resumo.conflitosEvitados]
    ];
    $('#painel-resumo').innerHTML = indicadores.map(([rotulo, valor, destaque]) =>
      `<div class="indicador${destaque ? ' destaque' : ''}"><small>${esc(rotulo)}</small><b>${esc(valor)}</b></div>`).join('');
    $('#tabela-auditoria tbody').innerHTML = auditoria.slice(0, 20).map((e) => `<tr>
        <td>${esc(dataHora(e.ocorridoEm))}</td><td>${esc(e.ator)}</td><td>${esc(e.acao)}</td>
        <td>${esc(e.entidade)} #${esc(e.entidadeId)}</td><td>${esc(e.estadoAnterior || '—')} → ${esc(e.estadoNovo || '—')}</td></tr>`).join('');
  }

  /** O mesmo formulário cadastra e edita; na edição, código e tipo ficam travados (não mudam depois de criados). */
  function modoEdicaoRecurso(recurso) {
    estado.editandoRecurso = recurso ? recurso.id : null;
    const form = $('#form-recurso');
    form.reset();
    form.classList.toggle('editando', Boolean(recurso));
    $('#recurso-codigo').disabled = Boolean(recurso);
    $('#recurso-tipo').disabled = Boolean(recurso);
    $('#cancelar-edicao').classList.toggle('d-none', !recurso);
    $('#salvar-recurso').textContent = recurso ? 'Salvar alterações' : 'Cadastrar';
    $('#form-recurso-titulo').textContent = recurso ? `Editar ${recurso.codigo}` : 'Cadastrar recurso';
    $('#form-recurso-ajuda').textContent = recurso
      ? 'Código e tipo não mudam depois do cadastro. As alterações valem para as próximas reservas.'
      : 'Salas, professores, materiais e equipamentos ficam disponíveis para reserva assim que cadastrados.';
    if (!recurso) return;
    $('#recurso-codigo').value = recurso.codigo;
    $('#recurso-tipo').value = recurso.tipo;
    $('#recurso-nome').value = recurso.nome;
    $('#recurso-capacidade').value = recurso.capacidade || '';
    $('#recurso-localizacao').value = recurso.localizacao || '';
    $('#recurso-descricao').value = recurso.descricao || '';
    $('#recurso-competencias').value = (recurso.competencias || []).join(', ');
    $('#recurso-restrito').checked = recurso.restrito;
    form.scrollIntoView({ block: 'start', behavior: 'smooth' });
    $('#recurso-nome').focus({ preventScroll: true });
  }

  $('#cancelar-edicao').addEventListener('click', () => modoEdicaoRecurso(null));

  $('#form-recurso').addEventListener('submit', (evento) => {
    evento.preventDefault();
    executar(async () => {
      const competencias = $('#recurso-competencias').value.split(',').map((c) => c.trim()).filter(Boolean);
      const dados = {
        codigo: $('#recurso-codigo').value, nome: $('#recurso-nome').value, tipo: $('#recurso-tipo').value,
        capacidade: $('#recurso-capacidade').value ? Number($('#recurso-capacidade').value) : null,
        localizacao: $('#recurso-localizacao').value || null, descricao: $('#recurso-descricao').value || null,
        restrito: $('#recurso-restrito').checked, competencias: competencias.length ? competencias : null
      };
      const editando = estado.editandoRecurso;
      const recurso = editando
        ? await api('PUT', `/api/v1/recursos/${editando}`, dados)
        : await api('POST', '/api/v1/recursos', dados);
      sucesso(editando ? `Recurso ${recurso.codigo} atualizado.` : `Recurso ${recurso.codigo} cadastrado (id ${recurso.id}).`);
      modoEdicaoRecurso(null);
      await carregarRecursosAdmin();
    });
  });

  $('#form-manutencao').addEventListener('submit', (evento) => {
    evento.preventDefault();
    executar(async () => {
      await api('POST', '/api/v1/manutencoes', {
        recursoId: Number($('#manutencao-recurso').value), inicio: paraApi($('#manutencao-inicio').value),
        fim: paraApi($('#manutencao-fim').value), motivo: $('#manutencao-motivo').value
      });
      sucesso('Manutenção registrada; o recurso está bloqueado no período.');
      evento.target.reset();
      await carregarManutencoes();
    });
  });

  $('#form-relatorio').addEventListener('submit', (evento) => {
    evento.preventDefault();
    executar(async () => {
      const janela = `inicio=${paraApi($('#relatorio-inicio').value)}&fim=${paraApi($('#relatorio-fim').value)}`;
      const [utilizacao, conflitos] = await Promise.all([
        api('GET', '/api/v1/relatorios/utilizacao?' + janela),
        api('GET', '/api/v1/relatorios/conflitos?' + janela)
      ]);
      $('#relatorio-criterio').textContent = utilizacao.criterio;
      $('#tabela-relatorio tbody').innerHTML = utilizacao.itens.map((i) => `<tr><td>${esc(i.codigo)}</td>
        <td>${esc(ROTULO_TIPO[i.tipo] || i.tipo)}</td><td>${i.reservas}</td><td>${i.horas}</td><td>${i.percentualOcupacao}%</td></tr>`).join('');
      $('#relatorio-conflitos').textContent = `Conflitos evitados no período: ${conflitos.total}.`;
    });
  });

  async function carregarRecursosAdmin() {
    const recursos = await api('GET', '/api/v1/recursos?incluirInativos=true');
    estado.recursosAdmin = new Map(recursos.map((r) => [r.id, r]));
    $('#lista-recursos-admin').innerHTML = recursos.length === 0
      ? vazio('Nenhum recurso cadastrado.')
      : SECOES.map((secao) => {
        const itens = recursos.filter((r) => r.tipo === secao.tipo);
        if (itens.length === 0) return '';
        return `<h3 class="lista-compacta-grupo">${icone(secao.icone)}${secao.titulo}<span class="contagem">${itens.length}</span></h3>
          ${itens.map((r) => `<div class="item-compacto${r.ativo ? '' : ' inativo'}">
            <div><strong>${esc(r.nome)}</strong><small>${esc(r.codigo)} · id ${r.id}${r.restrito ? ' · exige aprovação' : ''}${r.ativo ? '' : ' · desativado'}</small></div>
            <span class="item-acoes">
              <button type="button" class="btn btn-suave btn-pequeno" data-recurso-editar="${r.id}">Editar</button>
              <button type="button" class="btn btn-suave btn-pequeno" data-recurso-acao="${r.ativo ? 'desativar' : 'reativar'}" data-id="${r.id}">
                ${r.ativo ? 'Desativar' : 'Reativar'}</button></span></div>`).join('')}`;
      }).join('');
  }

  $('#lista-recursos-admin').addEventListener('click', (evento) => {
    const editar = evento.target.closest('button[data-recurso-editar]');
    if (editar) {
      modoEdicaoRecurso(estado.recursosAdmin.get(Number(editar.dataset.recursoEditar)));
      return;
    }
    const botao = evento.target.closest('button[data-recurso-acao]');
    if (!botao) return;
    executar(async () => {
      await api('POST', `/api/v1/recursos/${botao.dataset.id}/${botao.dataset.recursoAcao}`);
      sucesso(botao.dataset.recursoAcao === 'desativar' ? 'Recurso desativado.' : 'Recurso reativado.');
      await carregarRecursosAdmin();
    });
  });

  async function carregarManutencoes() {
    limitarDatas();
    const [recursos, manutencoes] = await Promise.all([
      api('GET', '/api/v1/recursos'),
      api('GET', '/api/v1/manutencoes?somenteAtivas=true')
    ]);
    const escolhido = $('#manutencao-recurso').value;
    $('#manutencao-recurso').innerHTML = '<option value="">Escolha o recurso</option>' + SECOES.map((secao) => {
      const itens = recursos.filter((r) => r.tipo === secao.tipo);
      return itens.length === 0 ? '' : `<optgroup label="${secao.titulo}">${itens.map((r) =>
        `<option value="${r.id}"${String(r.id) === escolhido ? ' selected' : ''}>${esc(r.codigo)} — ${esc(r.nome)}</option>`).join('')}</optgroup>`;
    }).join('');
    $('#lista-manutencoes').innerHTML = manutencoes.length === 0
      ? vazio('Nenhum recurso em manutenção.')
      : manutencoes.map((m) => `<div class="item-compacto">
          <div><strong>${esc(m.recursoNome)}</strong><small>${esc(dataHora(m.inicio))} – ${esc(dataHora(m.fim))} · ${esc(m.motivo)}</small></div>
          <button type="button" class="btn btn-suave btn-pequeno" data-cancelar-manutencao="${m.id}">Cancelar</button></div>`).join('');
  }

  $('#lista-manutencoes').addEventListener('click', (evento) => {
    const botao = evento.target.closest('button[data-cancelar-manutencao]');
    if (!botao) return;
    executar(async () => {
      await api('POST', `/api/v1/manutencoes/${botao.dataset.cancelarManutencao}/cancelar`);
      sucesso('Manutenção cancelada; o recurso está liberado.');
      await carregarManutencoes();
    });
  });

  // ------------------------------------------------------------------ avisos do usuário

  const chaveAvisos = () => `sor-avisos-vistos-${estado.usuario ? estado.usuario.username || estado.usuario.nome : ''}`;

  /** Mostra no menu quantos avisos chegaram desde a última vez que a pessoa abriu "Avisos". */
  async function verificarAvisos() {
    if (!estado.usuario) return;
    const lista = await api('GET', '/api/v1/notificacoes');
    const visto = Number(lerLocal(chaveAvisos()) || 0);
    const novos = lista.filter((n) => n.id > visto).length;
    const selo = $('#badge-avisos');
    const naAba = !$('#aba-notificacoes').classList.contains('d-none');
    selo.textContent = novos > 9 ? '9+' : String(novos);
    selo.classList.toggle('d-none', novos === 0 || naAba);
    selo.setAttribute('aria-label', `${novos} aviso(s) novo(s)`);
  }

  async function carregarNotificacoes() {
    const lista = await api('GET', '/api/v1/notificacoes');
    const visto = Number(lerLocal(chaveAvisos()) || 0);
    $('#lista-notificacoes').innerHTML = lista.length === 0
      ? vazio('Você não tem avisos por enquanto.')
      : lista.map((n) => `<div class="notificacao${n.id > visto ? ' nova' : ''}">${icone('sino')}<div>${esc(n.mensagem)}<br>
          <small>${esc(dataHora(n.criadaEm))}${n.id > visto ? ' · <b class="novo">novo</b>' : ''}</small></div></div>`).join('');
    const maior = lista.reduce((m, n) => Math.max(m, n.id), visto);
    gravarLocal(chaveAvisos(), String(maior));
    $('#badge-avisos').classList.add('d-none');
  }

  iniciar();
})();
