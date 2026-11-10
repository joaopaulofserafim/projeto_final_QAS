/*
 * Interface do Sistema de Organização de Recursos.
 * Sem framework: chama a API REST (/api/v1) com a sessão do navegador e o token CSRF (cookie XSRF-TOKEN).
 * Todo texto vindo da API é escapado antes de ir para o HTML.
 */
(function () {
  'use strict';

  const estado = { usuario: null, selecionados: new Set() };
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

  // ------------------------------------------------------------------ mensagens

  function mostrar(tipo, titulo, detalhe, itens) {
    const caixa = $('#mensagem');
    const lista = (itens || []).map((i) => `<li>${esc(i)}</li>`).join('');
    caixa.className = `alert alert-${tipo}`;
    caixa.setAttribute('role', tipo === 'danger' ? 'alert' : 'status');
    caixa.innerHTML = `<strong>${esc(titulo)}</strong> ${esc(detalhe || '')}${lista ? `<ul>${lista}</ul>` : ''}`;
    caixa.scrollIntoView({ block: 'nearest' });
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

  // ------------------------------------------------------------------ sessão

  function telaLogin() {
    estado.usuario = null;
    $('#tela-app').classList.add('d-none');
    $('#sessao').classList.add('d-none');
    $('#tela-login').classList.remove('d-none');
  }

  function telaApp(usuario) {
    estado.usuario = usuario;
    $('#usuario-atual').textContent = `${usuario.nome} (${usuario.perfil.toLowerCase()})`;
    $('#tela-login').classList.add('d-none');
    $('#sessao').classList.remove('d-none');
    $('#tela-app').classList.remove('d-none');
    document.querySelectorAll('[data-perfis]').forEach((el) => {
      const permitido = el.dataset.perfis.split(',').includes(usuario.perfil);
      (el.closest('li.nav-item') || el).classList.toggle('d-none', !permitido);
    });
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
    telaLogin();
    sucesso('Sessão encerrada.');
  }));

  // ------------------------------------------------------------------ abas

  const carregadores = {
    reservar: () => Promise.resolve(),
    minhas: carregarMinhas,
    aprovacoes: carregarAprovacoes,
    movimentacoes: carregarMovimentacoes,
    admin: carregarAdmin,
    notificacoes: carregarNotificacoes
  };

  function abrirAba(nome) {
    document.querySelectorAll('#abas .nav-link').forEach((b) => {
      const ativa = b.dataset.aba === nome;
      b.classList.toggle('active', ativa);
      b.setAttribute('aria-current', ativa ? 'page' : 'false');
    });
    document.querySelectorAll('.aba').forEach((s) => s.classList.toggle('d-none', s.id !== `aba-${nome}`));
    executar(carregadores[nome]);
  }

  $('#abas').addEventListener('click', (evento) => {
    const botao = evento.target.closest('[data-aba]');
    if (botao) abrirAba(botao.dataset.aba);
  });

  const nomesEstado = {
    SOLICITADA: ['Aguardando aprovação', 'warning'], APROVADA: ['Aprovada', 'success'], EM_USO: ['Em uso', 'primary'],
    CONCLUIDA: ['Concluída', 'secondary'], REJEITADA: ['Rejeitada', 'danger'], CANCELADA: ['Cancelada', 'dark'],
    NAO_COMPARECEU: ['Não compareceu', 'danger']
  };

  function selo(estadoReserva) {
    const [texto, cor] = nomesEstado[estadoReserva] || [estadoReserva, 'light'];
    return `<span class="badge text-bg-${cor} badge-estado" data-estado="${esc(estadoReserva)}">${esc(texto)}</span>`;
  }

  const periodo = (r) => `${dataHora(r.inicio)} – ${dataHora(r.fim).substring(11)}`;
  const listaRecursos = (r) => r.recursos.map((x) => esc(x.codigo)).join(', ');

  // ------------------------------------------------------------------ recursos e reservas

  $('#form-busca').addEventListener('submit', (evento) => {
    evento.preventDefault();
    executar(pesquisar);
  });

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
    estado.selecionados.clear();
    $('#tabela-recursos tbody').innerHTML = recursos.length === 0
      ? '<tr><td colspan="7">Nenhum recurso encontrado com esses filtros.</td></tr>'
      : recursos.map((r) => `<tr data-recurso="${r.id}">
          <td><input type="checkbox" class="form-check-input escolher" value="${r.id}" aria-label="Escolher ${esc(r.codigo)}"></td>
          <td>${esc(r.codigo)}${r.restrito ? ' <span class="badge text-bg-warning">exige aprovação</span>' : ''}</td>
          <td class="quebra">${esc(r.nome)}${r.competencias.length ? `<br><small>${esc(r.competencias.join(', '))}</small>` : ''}</td>
          <td>${esc(r.tipo)}</td><td>${esc(r.capacidade)}</td><td>${esc(r.localizacao)}</td>
          <td class="situacao">—</td></tr>`).join('');
  }

  $('#tabela-recursos').addEventListener('change', (evento) => {
    if (!evento.target.classList.contains('escolher')) return;
    const id = Number(evento.target.value);
    if (evento.target.checked) estado.selecionados.add(id); else estado.selecionados.delete(id);
  });

  function periodoEscolhido() {
    return { inicio: paraApi($('#busca-inicio').value), fim: paraApi($('#busca-fim').value) };
  }

  $('#botao-disponibilidade').addEventListener('click', () => executar(async () => {
    const { inicio, fim } = periodoEscolhido();
    if (estado.selecionados.size === 0 || !inicio || !fim) {
      mostrar('warning', 'Faltam dados.', 'Escolha ao menos um recurso e informe início e término.');
      return;
    }
    const ids = [...estado.selecionados].join(',');
    const situacoes = await api('GET', `/api/v1/disponibilidade?recursoIds=${ids}&inicio=${inicio}&fim=${fim}`);
    situacoes.forEach((s) => {
      const celula = document.querySelector(`tr[data-recurso="${s.recursoId}"] .situacao`);
      if (!celula) return;
      celula.innerHTML = s.disponivel
        ? '<span class="text-success">Livre</span>'
        : `<span class="text-danger">Ocupado</span><br><small>${s.motivos.map((m) => esc(motivo(m))).join('<br>')}</small>`;
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
      const { inicio, fim } = periodoEscolhido();
      const reserva = await api('POST', '/api/v1/reservas', {
        inicio, fim,
        finalidade: $('#reserva-finalidade').value,
        justificativa: $('#reserva-justificativa').value || null,
        recursoIds: [...estado.selecionados]
      });
      const situacao = reserva.estado === 'APROVADA' ? 'confirmada' : 'registrada e aguarda aprovação';
      sucesso(`Reserva #${reserva.id} ${situacao} (${nomesEstado[reserva.estado][0]}).`);
    });
  });

  // ------------------------------------------------------------------ minhas reservas

  async function carregarMinhas() {
    const reservas = await api('GET', '/api/v1/reservas/minhas');
    $('#tabela-minhas tbody').innerHTML = reservas.length === 0
      ? '<tr><td colspan="5">Você ainda não tem reservas.</td></tr>'
      : reservas.map((r) => `<tr data-reserva="${r.id}">
          <td>${r.id}</td><td>${esc(periodo(r))}</td><td class="quebra">${listaRecursos(r)}</td><td>${selo(r.estado)}</td>
          <td class="d-flex gap-1">
            ${['SOLICITADA', 'APROVADA'].includes(r.estado)
              ? `<button class="btn btn-sm btn-outline-danger" data-acao="cancelar" data-id="${r.id}">Cancelar</button>` : ''}
            <button class="btn btn-sm btn-outline-secondary" data-acao="historico" data-id="${r.id}">Histórico</button>
          </td></tr>`).join('');
  }

  $('#tabela-minhas').addEventListener('click', (evento) => {
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
        caixa.innerHTML = `<h3 class="h6">Histórico da reserva #${esc(id)}</h3><ol class="mb-0">${eventos.map((e) =>
          `<li>${esc(dataHora(e.ocorridoEm))} — ${esc(e.acao)} por ${esc(e.ator)}: ${esc(e.estadoAnterior || '—')} → ${esc(e.estadoNovo || '—')}</li>`).join('')}</ol>`;
      }
    });
  });

  // ------------------------------------------------------------------ aprovações e uso

  async function carregarAprovacoes() {
    const [fila, aprovadas, emUso] = await Promise.all([
      api('GET', '/api/v1/reservas?estado=SOLICITADA'),
      api('GET', '/api/v1/reservas?estado=APROVADA'),
      api('GET', '/api/v1/reservas?estado=EM_USO')
    ]);
    $('#tabela-fila tbody').innerHTML = fila.length === 0
      ? '<tr><td colspan="6">Nenhuma solicitação pendente.</td></tr>'
      : fila.map((r) => `<tr data-reserva="${r.id}">
          <td>${r.id}</td><td>${esc(r.solicitante.nome)}</td><td>${esc(periodo(r))}</td><td>${listaRecursos(r)}</td>
          <td class="quebra">${esc(r.justificativa)}</td>
          <td><div class="d-flex flex-wrap gap-1">
            <input class="form-control form-control-sm motivo" placeholder="Motivo" aria-label="Motivo da decisão da reserva ${r.id}">
            <button class="btn btn-sm btn-success" data-acao="aprovar" data-id="${r.id}">Aprovar</button>
            <button class="btn btn-sm btn-outline-danger" data-acao="rejeitar" data-id="${r.id}">Rejeitar</button>
          </div></td></tr>`).join('');
    const uso = aprovadas.concat(emUso);
    $('#tabela-uso tbody').innerHTML = uso.length === 0
      ? '<tr><td colspan="6">Nenhuma reserva aprovada ou em uso.</td></tr>'
      : uso.map((r) => `<tr data-reserva="${r.id}">
          <td>${r.id}</td><td>${esc(r.solicitante.nome)}</td><td>${esc(periodo(r))}</td><td>${listaRecursos(r)}</td>
          <td>${selo(r.estado)}</td><td class="d-flex gap-1">${r.estado === 'APROVADA'
            ? `<button class="btn btn-sm btn-primary" data-acao="iniciar" data-id="${r.id}">Iniciar uso</button>
               <button class="btn btn-sm btn-outline-danger" data-acao="nao-comparecimento" data-id="${r.id}">Não compareceu</button>`
            : `<button class="btn btn-sm btn-secondary" data-acao="concluir" data-id="${r.id}">Concluir</button>`}</td></tr>`).join('');
  }

  $('#aba-aprovacoes').addEventListener('click', (evento) => {
    const botao = evento.target.closest('button[data-acao]');
    if (!botao) return;
    const id = botao.dataset.id;
    const campo = botao.closest('tr').querySelector('.motivo');
    executar(async () => {
      const reserva = await api('POST', `/api/v1/reservas/${id}/${botao.dataset.acao}`,
        campo ? { motivo: campo.value || null } : undefined);
      sucesso(`Reserva #${id}: ${nomesEstado[reserva.estado][0]}.`);
      await carregarAprovacoes();
    });
  });

  // ------------------------------------------------------------------ retiradas

  async function carregarMovimentacoes() {
    const abertas = await api('GET', '/api/v1/movimentacoes?situacao=ABERTAS');
    $('#tabela-movimentacoes tbody').innerHTML = abertas.length === 0
      ? '<tr><td colspan="6">Nenhum material pendente de devolução.</td></tr>'
      : abertas.map((m) => `<tr>
          <td>${m.id}</td><td>${m.reservaId}</td><td>${esc(m.recursoCodigo)}</td><td>${esc(dataHora(m.prazoDevolucao))}</td>
          <td>${m.atrasada ? '<span class="badge text-bg-danger">Atrasada</span>' : '<span class="badge text-bg-success">No prazo</span>'}</td>
          <td><div class="d-flex flex-wrap gap-1 align-items-center">
            <label class="form-check-label small"><input type="checkbox" class="form-check-input avaria"> avaria</label>
            <button class="btn btn-sm btn-primary" data-devolver="${m.id}">Registrar devolução</button>
          </div></td></tr>`).join('');
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

  $('#tabela-movimentacoes').addEventListener('click', (evento) => {
    const botao = evento.target.closest('button[data-devolver]');
    if (!botao) return;
    const avaria = botao.closest('tr').querySelector('.avaria').checked;
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
      ['Aguardando aprovação', resumo.solicitacoesPendentes], ['Aprovadas', resumo.reservasPorEstado.APROVADA],
      ['Em uso', resumo.reservasPorEstado.EM_USO], ['Materiais pendentes', resumo.movimentacoesAbertas],
      ['Devoluções atrasadas', resumo.movimentacoesAtrasadas], ['Manutenções ativas', resumo.manutencoesAtivas],
      ['Conflitos evitados', resumo.conflitosEvitados]
    ];
    $('#painel-resumo').innerHTML = indicadores.map(([rotulo, valor]) => `<div class="col-6 col-md-3 col-xl">
        <div class="card card-body cartao-indicador"><span class="small text-muted">${esc(rotulo)}</span>
        <span class="valor">${esc(valor)}</span></div></div>`).join('');
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
        <td>${esc(i.tipo)}</td><td>${i.reservas}</td><td>${i.horas}</td><td>${i.percentualOcupacao}%</td></tr>`).join('');
      $('#relatorio-conflitos').textContent = `Conflitos evitados no período: ${conflitos.total}.`;
    });
  });

  // ------------------------------------------------------------------ notificações

  async function carregarNotificacoes() {
    const lista = await api('GET', '/api/v1/notificacoes');
    $('#lista-notificacoes').innerHTML = lista.length === 0
      ? '<li class="list-group-item">Sem notificações.</li>'
      : lista.map((n) => `<li class="list-group-item"><small class="text-muted">${esc(dataHora(n.criadaEm))}</small><br>
          ${esc(n.mensagem)}</li>`).join('');
  }

  iniciar();
})();
