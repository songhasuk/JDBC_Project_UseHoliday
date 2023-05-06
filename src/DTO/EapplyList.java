package dto;

import lombok.Data;

@Data
public class EapplyList {
	
	private int applyno;
	private int stateno;
	private int empno;
	private String vacationDays; //신청일수
	private String hname;
	private String reason;
	private String sinfo;
	private String restday;
	

}
