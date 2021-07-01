package org.zigmoi.ketchup.application.dtos;

import lombok.Data;
import org.zigmoi.ketchup.application.entities.ApplicationId;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Pattern;
import java.util.Date;

@Data
public class ApplicationBasicResponseDto {
    private ApplicationId id;

    @Pattern(regexp = "WEB-APPLICATION")
    private String type;

    private String displayName;
    private String data;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdOn;
    private String createdBy;
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdatedOn;
    private String lastUpdatedBy;

}
