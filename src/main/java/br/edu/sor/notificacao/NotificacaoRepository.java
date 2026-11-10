package br.edu.sor.notificacao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    List<Notificacao> findTop100ByDestinatarioIdOrderByIdDesc(Long destinatarioId);
}
