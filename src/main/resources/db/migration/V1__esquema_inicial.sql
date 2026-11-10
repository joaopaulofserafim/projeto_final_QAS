-- Esquema do Sistema de Organização de Recursos.
-- Decisões aplicadas: DP-001 (SOLICITADA ocupa horário), DP-016 (professor é recurso alocável),
-- DP-017 (PostgreSQL) e DP-018 (lock por recurso + constraint de exclusão como defesa final).

CREATE EXTENSION IF NOT EXISTS btree_gist;

CREATE TABLE usuario (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(60)  NOT NULL UNIQUE,
    nome        VARCHAR(120) NOT NULL,
    email       VARCHAR(160),
    senha_hash  VARCHAR(200) NOT NULL,
    perfil      VARCHAR(20)  NOT NULL CHECK (perfil IN ('SOLICITANTE', 'RESPONSAVEL', 'ADMINISTRADOR')),
    ativo       BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em   TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE recurso (
    id          BIGSERIAL PRIMARY KEY,
    codigo      VARCHAR(40)  NOT NULL UNIQUE,
    nome        VARCHAR(120) NOT NULL,
    tipo        VARCHAR(20)  NOT NULL CHECK (tipo IN ('SALA', 'PROFESSOR', 'MATERIAL', 'EQUIPAMENTO')),
    capacidade  INTEGER CHECK (capacidade IS NULL OR capacidade > 0),
    localizacao VARCHAR(120),
    descricao   VARCHAR(500),
    restrito    BOOLEAN      NOT NULL DEFAULT FALSE,
    ativo       BOOLEAN      NOT NULL DEFAULT TRUE,
    versao      BIGINT       NOT NULL DEFAULT 0
);

CREATE TABLE recurso_competencia (
    recurso_id  BIGINT      NOT NULL REFERENCES recurso (id),
    competencia VARCHAR(80) NOT NULL,
    PRIMARY KEY (recurso_id, competencia)
);

CREATE TABLE reserva (
    id              BIGSERIAL PRIMARY KEY,
    solicitante_id  BIGINT       NOT NULL REFERENCES usuario (id),
    inicio          TIMESTAMP    NOT NULL,
    fim             TIMESTAMP    NOT NULL,
    estado          VARCHAR(20)  NOT NULL CHECK (estado IN ('SOLICITADA', 'APROVADA', 'EM_USO', 'CONCLUIDA',
                                                          'REJEITADA', 'CANCELADA', 'NAO_COMPARECEU')),
    finalidade      VARCHAR(300) NOT NULL,
    justificativa   VARCHAR(500),
    motivo_decisao  VARCHAR(500),
    decidido_por_id BIGINT REFERENCES usuario (id),
    criada_em       TIMESTAMP    NOT NULL,
    atualizada_em   TIMESTAMP    NOT NULL,
    versao          BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT reserva_periodo_valido CHECK (fim > inicio)
);

CREATE TABLE manutencao (
    id            BIGSERIAL PRIMARY KEY,
    recurso_id    BIGINT       NOT NULL REFERENCES recurso (id),
    inicio        TIMESTAMP    NOT NULL,
    fim           TIMESTAMP    NOT NULL,
    motivo        VARCHAR(300) NOT NULL,
    ativa         BOOLEAN      NOT NULL DEFAULT TRUE,
    criada_por_id BIGINT       NOT NULL REFERENCES usuario (id),
    criada_em     TIMESTAMP    NOT NULL,
    CONSTRAINT manutencao_periodo_valido CHECK (fim > inicio)
);

-- Toda ocupação de horário (reserva ou manutenção) de um recurso.
-- A constraint de exclusão impede duas ocupações ativas sobrepostas no mesmo recurso,
-- mesmo que a aplicação falhe: é a última linha de defesa contra dupla reserva.
CREATE TABLE ocupacao (
    id            BIGSERIAL PRIMARY KEY,
    recurso_id    BIGINT      NOT NULL REFERENCES recurso (id),
    reserva_id    BIGINT REFERENCES reserva (id),
    manutencao_id BIGINT REFERENCES manutencao (id),
    tipo          VARCHAR(20) NOT NULL CHECK (tipo IN ('RESERVA', 'MANUTENCAO')),
    inicio        TIMESTAMP   NOT NULL,
    fim           TIMESTAMP   NOT NULL,
    ativa         BOOLEAN     NOT NULL,
    periodo       TSRANGE GENERATED ALWAYS AS (tsrange(inicio, fim, '[)')) STORED,
    CONSTRAINT ocupacao_periodo_valido CHECK (fim > inicio),
    CONSTRAINT ocupacao_origem_unica CHECK (
        (tipo = 'RESERVA' AND reserva_id IS NOT NULL AND manutencao_id IS NULL)
        OR (tipo = 'MANUTENCAO' AND manutencao_id IS NOT NULL AND reserva_id IS NULL)),
    CONSTRAINT ocupacao_sem_sobreposicao EXCLUDE USING gist (recurso_id WITH =, periodo WITH &&) WHERE (ativa)
);

CREATE INDEX ix_ocupacao_reserva ON ocupacao (reserva_id);
CREATE INDEX ix_ocupacao_manutencao ON ocupacao (manutencao_id);

CREATE TABLE movimentacao (
    id                   BIGSERIAL PRIMARY KEY,
    reserva_id           BIGINT    NOT NULL REFERENCES reserva (id),
    recurso_id           BIGINT    NOT NULL REFERENCES recurso (id),
    retirada_em          TIMESTAMP NOT NULL,
    retirado_por_id      BIGINT    NOT NULL REFERENCES usuario (id),
    observacao_retirada  VARCHAR(300),
    devolvida_em         TIMESTAMP,
    recebido_por_id      BIGINT REFERENCES usuario (id),
    observacao_devolucao VARCHAR(300),
    avaria               BOOLEAN   NOT NULL DEFAULT FALSE,
    CONSTRAINT movimentacao_unica_por_item UNIQUE (reserva_id, recurso_id)
);

-- Tentativas recusadas por conflito: fonte do relatório de "conflitos evitados" (DP-010).
CREATE TABLE conflito_evitado (
    id             BIGSERIAL PRIMARY KEY,
    recurso_id     BIGINT       NOT NULL REFERENCES recurso (id),
    solicitante_id BIGINT       NOT NULL REFERENCES usuario (id),
    inicio         TIMESTAMP    NOT NULL,
    fim            TIMESTAMP    NOT NULL,
    motivo         VARCHAR(40)  NOT NULL,
    ocorrido_em    TIMESTAMP    NOT NULL
);

CREATE INDEX ix_conflito_ocorrido ON conflito_evitado (ocorrido_em);

CREATE TABLE evento_auditoria (
    id              BIGSERIAL PRIMARY KEY,
    ocorrido_em     TIMESTAMP    NOT NULL,
    ator            VARCHAR(60)  NOT NULL,
    acao            VARCHAR(60)  NOT NULL,
    entidade        VARCHAR(30)  NOT NULL,
    entidade_id     BIGINT       NOT NULL,
    estado_anterior VARCHAR(30),
    estado_novo     VARCHAR(30),
    detalhes        VARCHAR(1000)
);

CREATE INDEX ix_auditoria_entidade ON evento_auditoria (entidade, entidade_id);

-- Histórico auditável protegido (RN-018): nenhuma alteração ou exclusão de evento.
CREATE FUNCTION impedir_alteracao_auditoria() RETURNS trigger AS
$$
BEGIN
    RAISE EXCEPTION 'Eventos de auditoria são imutáveis';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER auditoria_imutavel
    BEFORE UPDATE OR DELETE ON evento_auditoria
    FOR EACH ROW EXECUTE FUNCTION impedir_alteracao_auditoria();

CREATE TABLE notificacao (
    id             BIGSERIAL PRIMARY KEY,
    destinatario_id BIGINT       NOT NULL REFERENCES usuario (id),
    reserva_id     BIGINT REFERENCES reserva (id),
    tipo           VARCHAR(40)  NOT NULL,
    mensagem       VARCHAR(500) NOT NULL,
    status         VARCHAR(20)  NOT NULL CHECK (status IN ('ENVIADA', 'FALHA', 'SIMULADA')),
    erro           VARCHAR(300),
    criada_em      TIMESTAMP    NOT NULL
);

CREATE INDEX ix_notificacao_destinatario ON notificacao (destinatario_id);
