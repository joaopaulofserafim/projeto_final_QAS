-- Story 1.7: prazo de devolução registrado na retirada (DP-008: término da reserva).
ALTER TABLE movimentacao ADD COLUMN prazo_devolucao TIMESTAMP NOT NULL;
CREATE INDEX ix_movimentacao_abertas ON movimentacao (reserva_id) WHERE devolvida_em IS NULL;
