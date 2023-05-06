package dto;

import java.sql.Date;

import lombok.Data;

@Data
public class Eapply {
	
	private int applyno;
	private int empno;
	private int holidayno;
	private int stateno;
	private  Date start_date;
	private  Date end_date;
	private String reason;
	
	
	public void setStartDate(String hiredate) {
		   this.start_date = Date.valueOf(hiredate);
		}
	
	public void setEndDate(String hiredate) {
		   this.end_date = Date.valueOf(hiredate);
		}
	

}
