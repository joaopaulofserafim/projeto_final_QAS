-- Dados de demonstração (carregados fora do perfil de testes).
INSERT INTO recurso (codigo, nome, tipo, capacidade, localizacao, descricao, restrito) VALUES
    ('SALA-101', 'Sala 101', 'SALA', 40, 'Bloco A - 1º andar', 'Sala de aula com projetor fixo', FALSE),
    ('SALA-102', 'Sala 102', 'SALA', 25, 'Bloco A - 1º andar', 'Sala de aula', FALSE),
    ('LAB-INF-01', 'Laboratório de Informática 1', 'SALA', 30, 'Bloco B - térreo', '30 computadores', TRUE),
    ('AUD-01', 'Auditório Principal', 'SALA', 200, 'Bloco C', 'Eventos e palestras', TRUE),
    ('PROF-ANA', 'Profa. Ana Souza', 'PROFESSOR', NULL, 'Departamento de Computação', NULL, FALSE),
    ('PROF-CARLOS', 'Prof. Carlos Lima', 'PROFESSOR', NULL, 'Departamento de Computação', NULL, FALSE),
    ('PROF-BEATRIZ', 'Profa. Beatriz Rocha', 'PROFESSOR', NULL, 'Departamento de Engenharia', NULL, FALSE),
    ('PROJ-01', 'Projetor Epson', 'EQUIPAMENTO', NULL, 'Almoxarifado', NULL, FALSE),
    ('NOTE-01', 'Notebook Dell', 'EQUIPAMENTO', NULL, 'Almoxarifado', 'Uso mediante aprovação', TRUE),
    ('KIT-ARD-01', 'Kit Arduino', 'MATERIAL', NULL, 'Almoxarifado', '10 placas e sensores', FALSE),
    ('CABO-HDMI-01', 'Cabo HDMI 5m', 'MATERIAL', NULL, 'Almoxarifado', NULL, FALSE);

INSERT INTO recurso_competencia (recurso_id, competencia)
SELECT id, c FROM recurso, unnest(ARRAY['Java', 'Engenharia de Software']) AS c WHERE codigo = 'PROF-ANA';
INSERT INTO recurso_competencia (recurso_id, competencia)
SELECT id, c FROM recurso, unnest(ARRAY['Banco de Dados', 'Redes']) AS c WHERE codigo = 'PROF-CARLOS';
INSERT INTO recurso_competencia (recurso_id, competencia)
SELECT id, c FROM recurso, unnest(ARRAY['Testes de Software', 'Qualidade']) AS c WHERE codigo = 'PROF-BEATRIZ';
