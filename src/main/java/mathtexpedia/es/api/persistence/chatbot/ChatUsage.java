package mathtexpedia.es.api.persistence.chatbot;

import jakarta.persistence.*;
import lombok.Data;
import mathtexpedia.es.api.persistence.user.UserAccount;

import java.time.LocalDate;

@Entity
@Table(name = "chat_usage", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "usage_date"}),
        @UniqueConstraint(columnNames = {"identifier", "usage_date"})
})
@Data
public class ChatUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserAccount user;

    private String identifier; //If authenticated email, else IP

    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    @Column(name = "tokens_used", nullable = false)
    private int tokensUsed;

    @Column(name = "request_count", nullable = false)
    private int requestCount;
}
