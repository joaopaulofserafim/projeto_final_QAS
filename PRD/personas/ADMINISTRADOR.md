# Persona: Administrador

## 1. Identificacao

- **Perfil de acesso:** Administrador
- **Nome ficticio:** Luciana Prado
- **Funcao:** Administradora operacional do sistema de recursos academicos

## 2. Perfil profissional

Luciana mantem os cadastros e acessos do sistema e acompanha a integridade das informacoes operacionais. Seu acesso privilegiado apoia a administracao, mas nao elimina validacoes, regras de negocio ou necessidade de auditoria.

## 3. Contexto de uso

Usa a aplicacao para manter usuarios e recursos, consultar reservas e tratar indisponibilidades, como periodos de manutencao. Tambem analisa relatorios, inconsistencias e registros de auditoria para apoiar a continuidade da operacao.

## 4. Responsabilidades

- Gerenciar usuarios e permissoes conforme os perfis institucionais.
- Cadastrar e atualizar salas, professores, materiais e equipamentos.
- Administrar a indisponibilidade de recursos e periodos de manutencao.
- Consultar reservas, relatorios de utilizacao e registros de auditoria.
- Investigar inconsistencias e acompanhar situacoes que ameacem a integridade ou disponibilidade dos dados.
- Preservar rastreabilidade e acesso adequado as informacoes.

## 5. Objetivos principais

- Manter cadastros completos, corretos e sem duplicidades indevidas.
- Garantir que cada usuario tenha somente as permissoes correspondentes ao perfil.
- Consultar reservas e identificar conflitos ou inconsistencias.
- Bloquear recursos em manutencao e administrar seus periodos de indisponibilidade.
- Acompanhar utilizacao por meio de relatorios operacionais.
- Consultar alteracoes auditaveis e monitorar riscos a operacao.

## 6. Motivacoes

Luciana quer que a instituicao tenha dados consistentes e recursos corretamente representados, com acesso controlado e operacao previsivel. Precisa conseguir investigar alteracoes sem depender de registros informais.

## 7. Necessidades

- Visao administrativa de usuarios, recursos, reservas e manutencoes.
- Controles para cadastro e atualizacao com validacoes claras.
- Gestao de permissoes por perfil e atribuicao autorizada de responsaveis.
- Historico de alteracoes consultavel e vinculado a quem realizou a acao.
- Indicadores e relatorios para acompanhar utilizacao e localizar inconsistencias.
- Alertas ou indicacoes operacionais para conflitos e recursos indisponiveis.

## 8. Principais dores e dificuldades

- Dados incorretos, incompletos ou duplicados.
- Recursos cadastrados indevidamente ou sem informacao de manutencao.
- Usuarios com acesso acima ou abaixo do necessario.
- Alteracoes sem autoria ou historico confiavel.
- Baixa visibilidade da utilizacao e dos conflitos.
- Dificuldade para localizar inconsistencias e reconstituir o que ocorreu.

## 9. Comportamentos esperados

- Valida dados e escopo antes de cadastrar ou atualizar um recurso.
- Concede permissoes segundo necessidade e responsabilidade institucional.
- Registra indisponibilidade e periodos de manutencao antes que o recurso seja alocado.
- Consulta historico para investigar alteracoes, sem editar ou suprimir registros de auditoria.
- Usa relatorios para identificar tendencias e inconsistencias, respeitando limites de acesso a dados sensiveis.
- Trata operacoes privilegiadas com cuidado e confirma o impacto de alteracoes relevantes.

## 10. Funcionalidades desejadas

- Gestao de usuarios e permissoes.
- Cadastro e atualizacao de salas, professores, materiais e equipamentos.
- Consulta administrativa de reservas e disponibilidade.
- Bloqueio de recursos e gestao de periodos de manutencao.
- Consulta de relatorios operacionais e utilizacao.
- Consulta de registros de auditoria e indicacoes de inconsistencias.

## 11. Informacoes que precisa consultar

- Identificacao, estado e caracteristicas dos recursos cadastrados.
- Usuarios, perfis e permissoes vigentes.
- Reservas, estados, periodos e conflitos.
- Periodos de manutencao e indisponibilidade.
- Indicadores de utilizacao e registros de alteracoes com autoria e contexto.
- Informacoes necessarias a investigacao, sem ampliar a exposicao de dados sensiveis.

## 12. Acoes que precisa realizar

- Criar e atualizar usuarios, perfis e permissoes autorizados.
- Cadastrar e atualizar recursos institucionais.
- Marcar recursos como indisponiveis e registrar periodos de manutencao.
- Consultar reservas, relatorios e auditoria.
- Investigar inconsistencias e corrigir dados por operacoes permitidas e rastreaveis.

## 13. Nivel de acesso

Privilegiado para administracao operacional, autenticado e sujeito a autorizacao, validacao de regras e auditoria.

## 14. Permissoes

- Gerenciar usuarios, perfis e permissoes administrativas.
- Administrar cadastros de salas, professores, materiais e equipamentos.
- Consultar reservas, disponibilidade, relatorios e auditoria.
- Administrar bloqueios e periodos de manutencao.
- Executar atualizacoes administrativas permitidas, mantendo autoria e historico.

## 15. Restricoes

- Acesso privilegiado nao autoriza ignorar regras criticas, validacoes ou controles de seguranca.
- Nao pode eliminar a rastreabilidade nem alterar diretamente registros de auditoria.
- Nao pode tornar valido horario com termino anterior ou igual ao inicio, nem permitir sobreposicao aceita para o mesmo recurso.
- Recurso em manutencao continua indisponivel para reserva; administracao nao deve contornar o bloqueio sem que a manutencao seja formalmente encerrada conforme o processo aplicavel.
- A aprovacao de solicitacoes restritas cabe somente a responsaveis autorizados. O perfil administrador, por si so, nao concede essa aprovacao.
- Reservas iniciadas nao podem ser apagadas; ajustes administrativos devem respeitar o estado e deixar registro auditavel.
- Dados pessoais e sensiveis so devem ser consultados ou expostos quando necessario e autorizado.

## 16. Expectativas em relacao a aplicacao

Luciana espera controles administrativos consistentes, visibilidade sobre a operacao e dados confiaveis. Toda acao relevante deve ser validada, atribuida a quem a realizou e consultavel depois. O sistema deve indicar conflitos e manutencoes sem permitir que o privilegio administrativo produza reservas inconsistentes.

## 17. Exemplo pratico de utilizacao

Luciana recebe a informacao de que um equipamento precisa passar por manutencao. Localiza o cadastro, registra o periodo de indisponibilidade e verifica reservas afetadas. A aplicacao impede novas reservas conflitantes durante o bloqueio. Depois, Luciana consulta o relatorio de utilizacao e o historico de alteracoes para confirmar quem registrou o bloqueio e quais atualizacoes ocorreram.

## 18. Criterios importantes para a experiencia do usuario

- Painel administrativo objetivo, com consultas e operacoes agrupadas por responsabilidade.
- Validacoes e impactos de uma alteracao visiveis antes da confirmacao.
- Diferenciacao clara entre administrar cadastros e decidir solicitacoes de reserva.
- Historico pesquisavel e protegido contra alteracao ou exclusao.
- Relatorios legiveis, com filtros uteis e informacao atualizada.
- Acoes privilegiadas exigem contexto, confirmacao adequada e registro de autoria.

## 19. Relacao com as regras criticas do projeto

- **Tempo e concorrencia:** cadastros e manutencoes devem refletir disponibilidade real. Validacoes impedem periodo invalido, sobreposicao aceita e dupla aceitacao de solicitacoes simultaneas; a disponibilidade de professores tambem e relevante.
- **Manutencao:** administra bloqueio e periodo de manutencao; recurso indisponivel nao pode ser reservado durante esse periodo.
- **Aprovacao e estados:** consulta estados `SOLICITADA`, `APROVADA`, `EM_USO`, `CONCLUIDA`, `REJEITADA` e `CANCELADA`, mas nao assume aprovacao de recurso restrito apenas por ser administradora. Reserva iniciada nao pode ser apagada.
- **Auditoria:** consulta registros para rastrear alteracoes. Toda mudanca de estado deve gerar auditoria, e a administradora nao pode suprimir ou editar diretamente esses registros.
- **Seguranca:** administra permissoes, mas suas proprias acoes permanecem autenticadas, autorizadas, validadas e rastreaveis. Dados sensiveis nao devem ser expostos indevidamente.

## 20. Cenarios de utilizacao

### Cenario 1: Cadastrar uma sala

Luciana verifica se a sala ja existe, informa seus dados e salva o cadastro. A aplicacao valida os campos necessarios, evita duplicidade conforme as regras de cadastro e registra a autoria da alteracao. O novo recurso passa a ser consultavel segundo as permissoes dos usuarios.

### Cenario 2: Bloquear equipamento para manutencao

Luciana localiza um equipamento e registra seu periodo de manutencao. Antes de confirmar, verifica reservas que possam ser afetadas. Durante o bloqueio, novas reservas nao podem ser aceitas para o equipamento; a alteracao fica registrada para consulta posterior.

### Cenario 3: Consultar historico de uma reserva

Ao investigar uma divergencia, Luciana consulta a reserva e seu historico de alteracoes, identificando estados, autoria e contexto disponivel. Ela pode consultar os registros, mas nao os edita nem os remove. Qualquer correcao de dados operacionais ocorre por acao permitida e tambem rastreavel.

### Cenario 4: Revisar permissoes de usuario

Luciana verifica o perfil de um usuario e ajusta permissoes conforme sua funcao e autorizacao institucional. A aplicacao aplica controles de acesso e registra a mudanca, permitindo auditoria sem conceder ao solicitante ou ao responsavel acesso administrativo global.