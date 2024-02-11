package ddbcar.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamoDbBean
public class TargetDDBSchema {

    private Long id;
    private String updatedDate;
    private String message;
    private Boolean checkedItem;

    @DynamoDbPartitionKey
    @DynamoDbAttribute("Id")
    public Long getId() {
        return id;
    }

    @DynamoDbSortKey
    @DynamoDbAttribute("UpdatedDate")
    public String getUpdatedDate() {
        return updatedDate;
    }

    @DynamoDbAttribute("Message")
    public String getMessage() {
        return message;
    }

    @DynamoDbAttribute("CheckedItem")
    public Boolean getCheckedItem() {
        return checkedItem;
    }
}
