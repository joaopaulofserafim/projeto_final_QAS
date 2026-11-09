# Evidência TDD — Story 1.5 — fase RED

Data: 2026-11-09. Commit: test(1.5) RED (testes escritos antes de qualquer código de reservas).
Comando: ./mvnw test -Dtest='ReservaApiTest,ConcorrenciaReservaTest,ConstraintExclusaoTest'
Ambiente: Java 21.0.12.1, Maven 3.9.11, PostgreSQL 16 (Testcontainers).

```text
[ERROR] Tests run: 22, Failures: 20, Errors: 1, Skipped: 0, Time elapsed: 25.99 s <<< FAILURE! -- in br.edu.sor.reservas.ReservaApiTest
[ERROR] Tests run: 5, Failures: 5, Errors: 0, Skipped: 0, Time elapsed: 7.240 s <<< FAILURE! -- in br.edu.sor.reservas.ConcorrenciaReservaTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.351 s -- in br.edu.sor.reservas.ConstraintExclusaoTest
[ERROR] Tests run: 30, Failures: 25, Errors: 1, Skipped: 0

Falhas (primeira linha de cada):
br.edu.sor.reservas.ConcorrenciaReservaTest.pedidosCruzadosDeVariosRecursosNaoCausamDeadlock -- Time elapsed: 1.418 s <<< FAILURE!
java.lang.AssertionError: 
[nenhum erro 500 por deadlock] 
br.edu.sor.reservas.ConcorrenciaReservaTest.mesmoProfessorDisputadoPorSalasDiferentesFicaComUmaUnicaAlocacao -- Time elapsed: 1.559 s <<< FAILURE!
java.lang.AssertionError: 

br.edu.sor.reservas.ConcorrenciaReservaTest.pedidosSimultaneosComHorariosSobrepostosDiferentesNuncaGeramSobreposicaoNoBanco -- Time elapsed: 1.406 s <<< FAILURE!
java.lang.AssertionError: 

br.edu.sor.reservas.ConcorrenciaReservaTest.recursosIndependentesSaoReservadosEmParaleloSemConflitoFalso -- Time elapsed: 1.525 s <<< FAILURE!
java.lang.AssertionError: 

br.edu.sor.reservas.ConcorrenciaReservaTest.vinteSolicitacoesSimultaneasParaOMesmoHorarioProduzemUmaUnicaReserva -- Time elapsed: 1.316 s <<< FAILURE!
java.lang.AssertionError: 

br.edu.sor.reservas.ReservaApiTest.reservaDeRecursosComunsNasceAprovadaComSalaEProfessorEAuditada -- Time elapsed: 1.778 s <<< FAILURE!
org.opentest4j.AssertionFailedError: 

br.edu.sor.reservas.ReservaApiTest.cadaTentativaRecusadaPorConflitoEhRegistradaComoConflitoEvitado -- Time elapsed: 0.757 s <<< FAILURE!
org.opentest4j.AssertionFailedError: 

br.edu.sor.reservas.ReservaApiTest.recursoRestritoExigeJustificativaENasceSolicitada -- Time elapsed: 0.652 s <<< FAILURE!
org.opentest4j.AssertionFailedError: 

br.edu.sor.reservas.ReservaApiTest.minhasReservasFiltraPorEstado -- Time elapsed: 0.742 s <<< FAILURE!
java.lang.AssertionError: 

br.edu.sor.reservas.ReservaApiTest.solicitanteNaoVeReservaDeOutraPessoaMasResponsavelEAdministradorVeem -- Time elapsed: 0.579 s <<< ERROR!
java.lang.NullPointerException: Cannot invoke "com.fasterxml.jackson.databind.JsonNode.asLong()" because the return value of "com.fasterxml.jackson.databind.JsonNode.get(String)" is null
	at br.edu.sor.reservas.ReservaApiTest.solicitanteNaoVeReservaDeOutraPessoaMasResponsavelEAdministradorVeem(ReservaApiTest.java:192)
br.edu.sor.reservas.ReservaApiTest.recursoDesativadoNaoPodeSerReservado -- Time elapsed: 0.689 s <<< FAILURE!
org.opentest4j.AssertionFailedError: 

br.edu.sor.reservas.ReservaApiTest.camposObrigatoriosAusentesRetornam400ComCampos -- Time elapsed: 0.552 s <<< FAILURE!
org.opentest4j.AssertionFailedError: 

br.edu.sor.reservas.ReservaApiTest.professorNaoPodeSerAlocadoEmDuasSalasNoMesmoHorario -- Time elapsed: 0.650 s <<< FAILURE!
org.opentest4j.AssertionFailedError: 

br.edu.sor.reservas.ReservaApiTest.consultaDeDisponibilidadeAgendaEPesquisaRefletemAReserva -- Time elapsed: 0.900 s <<< FAILURE!
org.opentest4j.AssertionFailedError: 

br.edu.sor.reservas.ReservaApiTest.recursoComReservaFuturaNaoPodeSerDesativado -- Time elapsed: 0.665 s <<< FAILURE!
org.opentest4j.AssertionFailedError: 

br.edu.sor.reservas.ReservaApiTest.sobreposicaoNoMesmoRecursoEhRecusadaEAdjacenciaEhAceita(String, String, int)[1] -- Time elapsed: 0.629 s <<< FAILURE!
org.opentest4j.AssertionFailedError: 

br.edu.sor.reservas.ReservaApiTest.sobreposicaoNoMesmoRecursoEhRecusadaEAdjacenciaEhAceita(String, String, int)[2] -- Time elapsed: 0.634 s <<< FAILURE!
org.opentest4j.AssertionFailedError: 

br.edu.sor.reservas.ReservaApiTest.sobreposicaoNoMesmoRecursoEhRecusadaEAdjacenciaEhAceita(String, String, int)[3] -- Time elapsed: 0.637 s <<< FAILURE!
org.opentest4j.AssertionFailedError: 

br.edu.sor.reservas.ReservaApiTest.sobreposicaoNoMesmoRecursoEhRecusadaEAdjacenciaEhAceita(String, String, int)[4] -- Time elapsed: 0.607 s <<< FAILURE!
org.opentest4j.AssertionFailedError: 

br.edu.sor.reservas.ReservaApiTest.sobreposicaoNoMesmoRecursoEhRecusadaEAdjacenciaEhAceita(String, String, int)[5] -- Time elapsed: 0.626 s <<< FAILURE!
org.opentest4j.AssertionFailedError: 

br.edu.sor.reservas.ReservaApiTest.sobreposicaoNoMesmoRecursoEhRecusadaEAdjacenciaEhAceita(String, String, int)[6] -- Time elapsed: 0.603 s <<< FAILURE!
org.opentest4j.AssertionFailedError: 

br.edu.sor.reservas.ReservaApiTest.sobreposicaoNoMesmoRecursoEhRecusadaEAdjacenciaEhAceita(String, String, int)[7] -- Time elapsed: 0.600 s <<< FAILURE!
org.opentest4j.AssertionFailedError: 

br.edu.sor.reservas.ReservaApiTest.periodoInvalidoEhRecusadoComMensagemClara(String, String, String, int, String)[1] -- Time elapsed: 0.562 s <<< FAILURE!
org.opentest4j.AssertionFailedError: 

br.edu.sor.reservas.ReservaApiTest.periodoInvalidoEhRecusadoComMensagemClara(String, String, String, int, String)[2] -- Time elapsed: 0.513 s <<< FAILURE!
org.opentest4j.AssertionFailedError: 

br.edu.sor.reservas.ReservaApiTest.periodoInvalidoEhRecusadoComMensagemClara(String, String, String, int, String)[3] -- Time elapsed: 0.520 s <<< FAILURE!
org.opentest4j.AssertionFailedError: 

br.edu.sor.reservas.ReservaApiTest.pedidoComVariosRecursosEhTudoOuNada -- Time elapsed: 0.606 s <<< FAILURE!
org.opentest4j.AssertionFailedError: 

```

Leitura: 26 dos 30 testes falham porque `POST/GET /api/v1/reservas` ainda não existem (respostas 404 no lugar de 201/409). Passam 4: os 3 de `ConstraintExclusaoTest` (a constraint do banco veio na story 1.3 e é verificada aqui como característica) e `recursoInexistenteRetorna404`, que passa por acaso enquanto o endpoint não existe e só ganha significado depois da implementação.
