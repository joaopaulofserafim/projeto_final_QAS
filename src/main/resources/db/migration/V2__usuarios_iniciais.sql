-- Usuários iniciais, um por perfil (senha de demonstração: Senha@123, hash BCrypt custo 10).
-- Em uma implantação real as senhas devem ser trocadas pelo Administrador.
INSERT INTO usuario (username, nome, email, senha_hash, perfil) VALUES
    ('admin', 'Administrador do Sistema', 'admin@sor.local',
     '{bcrypt}$2a$10$j5Uz/aOIyU2xL5Gu1PdG8.CBOZdHefxKj0Nsqdl7DPShMoL2DpANK', 'ADMINISTRADOR'),
    ('responsavel', 'Rita Responsável', 'responsavel@sor.local',
     '{bcrypt}$2a$10$j5Uz/aOIyU2xL5Gu1PdG8.CBOZdHefxKj0Nsqdl7DPShMoL2DpANK', 'RESPONSAVEL'),
    ('solicitante', 'Marina Costa', 'marina@sor.local',
     '{bcrypt}$2a$10$j5Uz/aOIyU2xL5Gu1PdG8.CBOZdHefxKj0Nsqdl7DPShMoL2DpANK', 'SOLICITANTE'),
    ('solicitante2', 'Paulo Mendes', 'paulo@sor.local',
     '{bcrypt}$2a$10$j5Uz/aOIyU2xL5Gu1PdG8.CBOZdHefxKj0Nsqdl7DPShMoL2DpANK', 'SOLICITANTE');
