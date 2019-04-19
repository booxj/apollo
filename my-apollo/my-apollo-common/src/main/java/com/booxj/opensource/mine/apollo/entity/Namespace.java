package com.booxj.opensource.mine.apollo.entity;

import com.booxj.opensource.mine.apollo.utils.InputValidator;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;


@Entity
@Table(name = "namespace")
@SQLDelete(sql = "update namespace set is_deleted = 1 where id = ?")
@Where(clause = "is_deleted = 0")
public class Namespace extends BaseEntity {

    @NotBlank(message = "name cannot be blank")
    @Pattern(
            regexp = InputValidator.CLUSTER_NAMESPACE_VALIDATOR,
            message = "Namespace格式错误: " + InputValidator.INVALID_CLUSTER_NAMESPACE_MESSAGE + " & " + InputValidator.INVALID_NAMESPACE_NAMESPACE_MESSAGE
    )
    private String name;

    @Column(name = "comment")
    private String comment;

    /**
     * 是否发布
     *
     * @return
     */
    @Column(name = "is_public", columnDefinition = "Bit default '0'")
    private boolean isPublic = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean aPublic) {
        isPublic = aPublic;
    }
}
