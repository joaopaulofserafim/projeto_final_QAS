/*
 * Interface do Portal de Recursos.
 * Sem framework: chama a API REST (/api/v1) com a sessão do navegador e o token CSRF (cookie XSRF-TOKEN).
 * Todo texto vindo da API é escapado antes de ir para o HTML (sem XSS) e nada usa script inline (CSP).
 */
(function () {
  'use strict';

  const estado = { usuario: null, selecionados: new Set(), vistos: new Map(), temporizador: null };
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
    caixa.innerHTML = `<strong>${esc(titulo)}</strong> ${esc(detalhe || '')}${lista ? `<ul>${lista}</ul>` : ''}`;
    clearTimeout(estado.temporizador);
    estado.temporizador = setTimeout(() => { caixa.className = 'd-none'; }, 9000);
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

  function telaLogin() {
    estado.usuario = null;
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
        mostrar('danger', 'Falha no login.', (corpo && corpo.detail) || 'Usuário ou senha inválidos.');
        return;
      }
      $('#mensagem').className = 'd-none';
      telaApp(await api('GET', '/api/v1/auth/me'));
    });
  });

  $('#botao-sair').addEventListener('click', () => executar(async () => {
    await api('POST', '/api/v1/auth/logout');
    estado.selecionados.clear();
    telaLogin();
    sucesso('Sessão encerrada.');
  }));

  // ------------------------------------------------------------------ abas

  const TITULOS = {
    reservar: 'Reservar', minhas: 'Minhas reservas', aprovacoes: 'Aprovações e uso',
    movimentacoes: 'Retiradas e devoluções', admin: 'Administração', notificacoes: 'Avisos'
  };

  const carregadores = {
    reservar: () => Promise.resolve(),
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

  function sugerirPeriodo() {
    if (!$('#busca-inicio').value) {
      $('#busca-inicio').value = amanha(10);
      $('#busca-fim').value = amanha(12);
    }
    resumirPeriodo();
  }

  $('#aba-reservar').addEventListener('click', (evento) => {
    const botao = evento.target.closest('[data-preset]');
    if (!botao) return;
    const [de, ate] = botao.dataset.preset === 'manha' ? [8, 12] : [14, 18];
    $('#busca-inicio').value = amanha(de);
    $('#busca-fim').value = amanha(ate);
    resumirPeriodo();
  });

  function resumirPeriodo() {
    const inicio = paraApi($('#busca-inicio').value);
    const fim = paraApi($('#busca-fim').value);
    $('#resumo-periodo').textContent = inicio && fim
      ? `${dataHora(inicio).substring(0, 10)} · ${dataHora(inicio).substring(11)} às ${dataHora(fim).substring(11)}`
      : 'Informe o início e o término na busca.';
  }
  $('#busca-inicio').addEventListener('change', resumirPeriodo);
  $('#busca-fim').addEventListener('change', resumirPeriodo);

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
    return `<label class="recurso-card tipo-${esc(r.tipo)}${marcado ? ' escolhido' : ''}" data-recurso="${r.id}">
      <span class="rc-topo"><span class="rc-tipo">${esc(ROTULO_TIPO[r.tipo] || r.tipo)}</span>
        <input type="checkbox" class="form-check-input escolher" value="${r.id}" aria-label="Escolher ${esc(r.codigo)}"${marcado ? ' checked' : ''}></span>
      <strong class="rc-nome">${esc(r.nome)}</strong>
      <span class="rc-codigo">${esc(r.codigo)}</span>
      <span class="rc-meta">${meta.map(([k, v]) => `<span><b>${k}</b>${v}</span>`).join('')}</span>
      <span class="rc-tags">${tags}</span>
      <span class="situacao"></span>
    </label>`;
  }

  async function pesquisar() {
    const filtros = new URLSearchParams();
    const campos = { tipo: '#busca-tipo', texto: '#busca-texto', capacidadeMinima: '#busca-capacidade',
      localizacao: '#busca-localizacao', competencia: '#busca-competencia' };
    Object.entries(campos).forEach(([chave, seletor]) => { if ($(seletor).value) filtros.set(chave, $(seletor).value); });
    const inicio = paraApi($('#busca-inicio').value);
    const fim = paraApi($('#busca-fim').value);
    if ($('#busca-livres').checked && inicio && fim) {
      filtros.set('disponivelInicio', inicio);
      filtros.set('disponivelFim', fim);
    }
    const recursos = await api('GET', '/api/v1/recursos?' + filtros);
    recursos.forEach((r) => estado.vistos.set(r.id, r));
    $('#grade-recursos').innerHTML = recursos.length === 0
      ? vazio('Nenhum recurso encontrado com esses filtros.')
      : recursos.map(cartaoRecurso).join('');
    atualizarPainel();
  }

  $('#grade-recursos').addEventListener('change', (evento) => {
    if (!evento.target.classList.contains('escolher')) return;
    const id = Number(evento.target.value);
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

  $('#botao-disponibilidade').addEventListener('click', () => executar(async () => {
    const inicio = paraApi($('#busca-inicio').value);
    const fim = paraApi($('#busca-fim').value);
    if (estado.selecionados.size === 0 || !inicio || !fim) {
      mostrar('warning', 'Faltam dados.', 'Escolha ao menos um recurso e informe início e término.');
      return;
    }
    const ids = [...estado.selecionados].join(',');
    const situacoes = await api('GET', `/api/v1/disponibilidade?recursoIds=${ids}&inicio=${inicio}&fim=${fim}`);
    situacoes.forEach((s) => {
      const celula = document.querySelector(`.recurso-card[data-recurso="${s.recursoId}"] .situacao`);
      if (!celula) return;
      celula.innerHTML = s.disponivel
        ? '<span class="livre">Livre neste período</span>'
        : `<span class="ocupado">Ocupado</span><br><small>${s.motivos.map((m) => esc(motivo(m))).join('<br>')}</small>`;
    });
  }));

  function motivo(m) {
    if (m.motivo === 'RECURSO_INATIVO') return 'Recurso desativado';
    const tipo = m.motivo === 'MANUTENCAO' ? 'Manutenção' : 'Reservado';
    return `${tipo}: ${dataHora(m.inicio)} – ${dataHora(m.fim).substring(11)}`;
  }

  $('#form-reserva').addEventListener('submit', (evento) => {
    evento.preventDefault();
    executar(async () => {
      const reserva = await api('POST', '/api/v1/reservas', {
        inicio: paraApi($('#busca-inicio').value), fim: paraApi($('#busca-fim').value),
        finalidade: $('#reserva-finalidade').value,
        justificativa: $('#reserva-justificativa').value || null,
        recursoIds: [...estado.selecionados]
      });
      const situacao = reserva.estado === 'APROVADA' ? 'confirmada' : 'registrada e aguarda aprovação';
      sucesso(`Reserva #${reserva.id} ${situacao} (${nomesEstado[reserva.estado]}).`);
    });
  });

  // ------------------------------------------------------------------ minhas reservas

  async function carregarMinhas() {
    const reservas = await api('GET', '/api/v1/reservas/minhas');
    $('#lista-minhas').innerHTML = reservas.length === 0
      ? vazio('Você ainda não tem reservas. Escolha um recurso em <strong>Reservar</strong>.')
      : reservas.map((r) => cartaoReserva(r, { acoes:
          (['SOLICITADA', 'APROVADA'].includes(r.estado)
            ? `<button class="btn btn-perigo btn-pequeno" data-acao="cancelar" data-id="${r.id}">Cancelar</button>` : '')
          + `<button class="btn btn-suave btn-pequeno" data-acao="historico" data-id="${r.id}">Histórico</button>` })).join('');
  }

  $('#lista-minhas').addEventListener('click', (evento) => {
    const botao = evento.target.closest('button[data-acao]');
    if (!botao) return;
    const id = botao.dataset.id;
    executar(async () => {
      if (botao.dataset.acao === 'cancelar') {
        await api('POST', `/api/v1/reservas/${id}/cancelar`, { motivo: 'Cancelada pelo solicitante' });
        sucesso(`Reserva #${id} cancelada.`);
        await carregarMinhas();
      } else {
        const eventos = await api('GET', `/api/v1/reservas/${id}/historico`);
        const caixa = $('#historico');
        caixa.classList.remove('d-none');
        caixa.innerHTML = `<h2 class="cartao-titulo">Histórico da reserva #${esc(id)}</h2><ol class="mb-0">${eventos.map((e) =>
          `<li>${esc(dataHora(e.ocorridoEm))} — ${esc(e.acao)} por ${esc(e.ator)}: ${esc(e.estadoAnterior || '—')} → ${esc(e.estadoNovo || '—')}</li>`).join('')}</ol>`;
        caixa.scrollIntoView({ block: 'nearest' });
      }
    });
  });

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
      : fila.map((r) => cartaoReserva(r, { decisao: DECISAO.replace('data-acao="aprovar"', `data-acao="aprovar" data-id="${r.id}"`)
          .replace('data-acao="rejeitar"', `data-acao="rejeitar" data-id="${r.id}"`) })).join('');
    const uso = aprovadas.concat(emUso);
    $('#lista-uso').innerHTML = uso.length === 0
      ? vazio('Nenhuma reserva aprovada ou em uso.')
      : uso.map((r) => cartaoReserva(r, { acoes: r.estado === 'APROVADA'
          ? `<button class="btn btn-suave btn-pequeno" data-acao="iniciar" data-id="${r.id}">Iniciar uso</button>
             <button class="btn btn-perigo btn-pequeno" data-acao="nao-comparecimento" data-id="${r.id}">Não compareceu</button>`
          : `<button class="btn btn-suave btn-pequeno" data-acao="concluir" data-id="${r.id}">Concluir</button>` })).join('');
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

  $('#form-retirada').addEventListener('submit', (evento) => {
    evento.preventDefault();
    executar(async () => {
      await api('POST', '/api/v1/movimentacoes/retiradas', {
        reservaId: Number($('#retirada-reserva').value), recursoId: Number($('#retirada-recurso').value)
      });
      sucesso('Retirada registrada.');
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

  async function carregarAdmin() {
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

  $('#form-recurso').addEventListener('submit', (evento) => {
    evento.preventDefault();
    executar(async () => {
      const competencias = $('#recurso-competencias').value.split(',').map((c) => c.trim()).filter(Boolean);
      const recurso = await api('POST', '/api/v1/recursos', {
        codigo: $('#recurso-codigo').value, nome: $('#recurso-nome').value, tipo: $('#recurso-tipo').value,
        capacidade: $('#recurso-capacidade').value ? Number($('#recurso-capacidade').value) : null,
        localizacao: $('#recurso-localizacao').value || null, restrito: $('#recurso-restrito').checked,
        competencias: competencias.length ? competencias : null
      });
      sucesso(`Recurso ${recurso.codigo} cadastrado (id ${recurso.id}).`);
      evento.target.reset();
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
      await carregarAdmin();
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

  // ------------------------------------------------------------------ avisos do usuário

  async function carregarNotificacoes() {
    const lista = await api('GET', '/api/v1/notificacoes');
    $('#lista-notificacoes').innerHTML = lista.length === 0
      ? vazio('Você não tem avisos por enquanto.')
      : lista.map((n) => `<div class="notificacao">${icone('sino')}<div>${esc(n.mensagem)}<br>
          <small>${esc(dataHora(n.criadaEm))}</small></div></div>`).join('');
  }

  iniciar();
})();
