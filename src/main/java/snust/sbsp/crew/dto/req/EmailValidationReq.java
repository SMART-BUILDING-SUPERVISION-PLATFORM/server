package snust.sbsp.crew.dto.req;

import com.sun.istack.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class EmailValidationReq {

  private String email;

  private String code;
}
