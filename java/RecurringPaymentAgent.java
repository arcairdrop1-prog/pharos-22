import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RecurringPaymentAgent {

    public enum PaymentStatus {
        PAID, PENDING, FAILED
    }

    public static class PaymentRecord {
        int installmentNumber;
        LocalDateTime scheduledTime;
        double amount;
        String token;
        String recipient;
        PaymentStatus status;
        String transactionHashOrError;

        public PaymentRecord(int installmentNumber, LocalDateTime scheduledTime, double amount, String token, String recipient, PaymentStatus status, String details) {
            this.installmentNumber = installmentNumber;
            this.scheduledTime = scheduledTime;
            this.amount = amount;
            this.token = token;
            this.recipient = recipient;
            this.status = status;
            this.transactionHashOrError = details;
        }

        @Override
        public String toString() {
            String timeStr = scheduledTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            return String.format("[#%d | %s] Destinatário: %s | Montante: %.2f %s | Estado: %s | Detalhes: %s",
                    installmentNumber, timeStr, recipient, amount, token, status, transactionHashOrError);
        }
    }

    private final String scheduleType; // "WEEKLY" ou "MONTHLY"
    private final double amount;
    private final String token;
    private final String recipient;
    private final List<PaymentRecord> paymentHistory = new ArrayList<>();
    private LocalDateTime nextScheduledPayment;

    // Requisito 01: Suporta agendamento (semanal ou mensal) com montante, token e destinatário[cite: 14]
    public RecurringPaymentAgent(String scheduleType, double amount, String token, String recipient, LocalDateTime firstPaymentTime) {
        this.scheduleType = scheduleType;
        this.amount = amount;
        this.token = token;
        this.recipient = recipient;
        this.nextScheduledPayment = firstPaymentTime;
    }

    // Requisitos 02 & 03: Prepara o pagamento e lida com saldo insuficiente/falhas sem duplicar[cite: 14]
    public void executeCycle(double userBalance, boolean simulateFailure, String txHashOrError) {
        System.out.println("\n--- A Executar Ciclo de Pagamento Recorrente ---");
        System.out.println("Próximo pagamento agendado para: " + nextScheduledPayment);

        // Requisito 03: Verificação de saldo insuficiente
        if (userBalance < amount) {
            String errorMsg = "FALHOU: Saldo insuficiente (Disponível: " + userBalance + " " + token + ", Necessário: " + amount + " " + token + ")";
            paymentHistory.add(new PaymentRecord(paymentHistory.size() + 1, nextScheduledPayment, amount, token, recipient, PaymentStatus.FAILED, errorMsg));
            System.out.println("❌ " + errorMsg);
            advanceSchedule();
            return;
        }

        if (simulateFailure) {
            String errorMsg = "FALHOU: Erro na execução da transação on-chain (" + txHashOrError + ")";
            paymentHistory.add(new PaymentRecord(paymentHistory.size() + 1, nextScheduledPayment, amount, token, recipient, PaymentStatus.FAILED, errorMsg));
            System.out.println("❌ " + errorMsg);
            advanceSchedule();
            return;
        }

        // Execução bem-sucedida (Requisito 02: Preparado e rastreado como pago)
        String successDetails = "Submetido com sucesso. TxHash: " + txHashOrError;
        paymentHistory.add(new PaymentRecord(paymentHistory.size() + 1, nextScheduledPayment, amount, token, recipient, PaymentStatus.PAID, successDetails));
        System.out.println("✅ " + successDetails);

        advanceSchedule();
    }

    private void advanceSchedule() {
        if (scheduleType.equalsIgnoreCase("WEEKLY")) {
            nextScheduledPayment = nextScheduledPayment.plusWeeks(1);
        } else if (scheduleType.equalsIgnoreCase("MONTHLY")) {
            nextScheduledPayment = nextScheduledPayment.plusMonths(1);
        }
    }

    // Requisito 04: Mostra o histórico de pagamentos e o próximo pagamento agendado[cite: 14]
    public void generateReport() {
        System.out.println("\n============================================================");
        System.out.println("📊 RELATÓRIO DO AGENTE DE PAGAMENTOS RECORRENTES");
        System.out.println("============================================================");
        System.out.println("Configuração: Frequência: " + scheduleType + " | Destino: " + recipient + " | Montante: " + amount + " " + token);
        System.out.println("⏰ Próximo Pagamento Agendado: " + nextScheduledPayment.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        
        System.out.println("\nHistórico de Pagamentos:");
        if (paymentHistory.isEmpty()) {
            System.out.println(" - Nenhum pagamento registado ainda.");
        } else {
            for (PaymentRecord record : paymentHistory) {
                System.out.println(" - " + record);
            }
        }
        System.out.println("============================================================");
    }

    public static void main(String[] args) {
        LocalDateTime startTime = LocalDateTime.now().plusMinutes(1);
        RecurringPaymentAgent agent = new RecurringPaymentAgent("MONTHLY", 1200.0, "USDC", "0xLandlord123", startTime);

        System.out.println("=== INÍCIO DA SIMULAÇÃO DO RECURRING PAYMENT AGENT ===");

        // Ciclo 1: Pagamento bem-sucedido
        agent.executeCycle(5000.0, false, "0xhash_pagamento_1");

        // Ciclo 2: Falha por saldo insuficiente (Requisito 03)
        agent.executeCycle(500.0, false, null);

        // Ciclo 3: Pagamento bem-sucedido após o ciclo anterior (sem duplo pagamento)
        agent.executeCycle(3000.0, false, "0xhash_pagamento_3");

        // Apresentação do relatório final (Requisito 04)
        agent.generateReport();
    }
}
