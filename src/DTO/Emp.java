package dto;

import java.sql.Date;
import lombok.Data;

@Data
public class Emp {
	
	private int empno;
	private int rankno;
	private int deptno;
	private int mgr;
	private String ename;
	private String id_number;
	private int age;
	private String tel;
	private Date hiredate;
	private String email;
	private String addr;
	
	
	public void setHiredate(String hiredate) {
		   this.hiredate = Date.valueOf(hiredate);
		}

}
