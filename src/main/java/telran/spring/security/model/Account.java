package telran.spring.security.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
public class Account implements Serializable{

	private static final long serialVersionUID = 1L;
	@Size(min = 5)
	final String userName;
	@Size(min = 5)
	final String password;
	LocalDateTime expDate;
	@Size(min = 1)
	final String[] roles;
	
}
