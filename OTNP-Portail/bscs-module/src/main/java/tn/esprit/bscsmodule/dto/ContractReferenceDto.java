import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractReferenceDto {
    private Long coId;
    private String coIdPub;
    private String dirNum;
}