package br.edu.sor.disponibilidade;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.MonthDay;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import br.edu.sor.compartilhado.erro.EntradaInvalidaException;

/**
 * RN-031: não há reservas em feriados. Os feriados nacionais (fixos e os que dependem da Páscoa) são calculados
 * para qualquer ano; a instituição acrescenta os seus em {@code sor.calendario.feriados-extras}.
 */
@Component
public class CalendarioAcademico {

    private static final List<MonthDay> FIXOS = List.of(
            MonthDay.of(1, 1),    // Confraternização Universal
            MonthDay.of(4, 21),   // Tiradentes
            MonthDay.of(5, 1),    // Dia do Trabalho
            MonthDay.of(9, 7),    // Independência
            MonthDay.of(10, 12),  // Nossa Senhora Aparecida
            MonthDay.of(11, 2),   // Finados
            MonthDay.of(11, 15),  // Proclamação da República
            MonthDay.of(11, 20),  // Dia Nacional de Zumbi e da Consciência Negra
            MonthDay.of(12, 25)); // Natal

    private final boolean nacionais;
    private final Set<LocalDate> extras;

    public CalendarioAcademico(@Value("${sor.calendario.feriados-nacionais:true}") boolean nacionais,
            @Value("${sor.calendario.feriados-extras:}") String extras) {
        this.nacionais = nacionais;
        this.extras = new HashSet<>();
        Arrays.stream(extras.split(",")).map(String::trim).filter(s -> !s.isEmpty())
                .map(LocalDate::parse).forEach(this.extras::add);
    }

    public boolean ehFeriado(LocalDate dia) {
        return extras.contains(dia) || (nacionais && ehFeriadoNacional(dia));
    }

    /** Feriados do ano em ordem, para a interface avisar antes de o usuário enviar o pedido. */
    public List<LocalDate> feriadosDoAno(int ano) {
        LocalDate fim = LocalDate.of(ano, 12, 31);
        return LocalDate.of(ano, 1, 1).datesUntil(fim.plusDays(1)).filter(this::ehFeriado).toList();
    }

    /** Recusa o período se algum dia que ele toca for feriado (um término às 00:00 não conta o dia seguinte). */
    public void garantirDiasLetivos(PeriodoReserva periodo) {
        LocalDate ultimo = periodo.termino().toLocalTime().equals(LocalTime.MIDNIGHT)
                ? periodo.termino().toLocalDate().minusDays(1)
                : periodo.termino().toLocalDate();
        for (LocalDate dia = periodo.inicio().toLocalDate(); !dia.isAfter(ultimo); dia = dia.plusDays(1)) {
            if (ehFeriado(dia)) {
                throw new EntradaInvalidaException(String.format(
                        "%02d/%02d/%d é feriado; não há reservas nesse dia. Escolha outra data.",
                        dia.getDayOfMonth(), dia.getMonthValue(), dia.getYear()));
            }
        }
    }

    static boolean ehFeriadoNacional(LocalDate dia) {
        if (FIXOS.contains(MonthDay.from(dia))) {
            return true;
        }
        LocalDate pascoa = pascoa(dia.getYear());
        return dia.equals(pascoa.minusDays(48))      // segunda de Carnaval
                || dia.equals(pascoa.minusDays(47))  // terça de Carnaval
                || dia.equals(pascoa.minusDays(2))   // Sexta-feira Santa
                || dia.equals(pascoa.plusDays(60));  // Corpus Christi
    }

    /** Domingo de Páscoa pelo algoritmo de Meeus/Jones/Butcher (calendário gregoriano). */
    static LocalDate pascoa(int ano) {
        int a = ano % 19;
        int b = ano / 100;
        int c = ano % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int mes = (h + l - 7 * m + 114) / 31;
        int dia = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(ano, mes, dia);
    }
}
