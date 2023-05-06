package DAO;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import DTO.Dept;
import DTO.EapplyList;
import DTO.Emp;
import DTO.Estate;
import DTO.HoliDay;
import Utils.SingletonHelper;
import lombok.Data;

@Data
public class UseHoliday {
	
	Connection conn = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	
	
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	Scanner sc = new Scanner(System.in);
	Emp emp;
	
	//로그인 
	public boolean logIn(int empno, String id_number) {
		
		String formatstr = id_number.substring(0, 6) + "-" + id_number.substring(6);
		
		
		
		try {
			conn = SingletonHelper.getConnection("oracle");
			String sql = "select ename, empno from emp where empno = ? and id_number like ? ";
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setInt(1, empno);
			pstmt.setString(2, "%"+formatstr+"%");
			
			rs = pstmt.executeQuery();
			
			
			
			if(rs.next()) {
				emp = new Emp();
				emp.setEname(rs.getString(1));
				emp.setEmpno(rs.getInt(2));
			}
	
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}finally {
			SingletonHelper.close(rs);
			SingletonHelper.close(pstmt);
		}
		
		if(emp  != null) {
			return true;
		}else {
			return false;
		}
	}
	
	//로그아웃
	public void logOut() {
		if(emp != null) {
		emp = null;
		System.out.println("로그아웃 되었습니다.");
		}
	}
	
	//휴가 신청 목록
	public Map<Integer, EapplyList> vacationRequestList() {
		
		Map<Integer, EapplyList> elLists = new LinkedHashMap<>();
		try {
			conn = SingletonHelper.getConnection("oracle");
			String sql = "select e.applyno, s.stateno, e2.empno, "
					+ " (to_char(e.end_date, 'FMDD')-to_char(e.start_date,'FMDD')+1) AS 신청일수, "
					+ " h.hname , e.reason, s.sinfo, r.restday "
					+ " from eapply e join holiday h on e.holidayno = h.holidayno " 
					+ " join estate s on e.stateno = s.stateno "
					+ " join rest_holiday r on e.empno = r.empno"
					+ " join emp e2 on e.empno = e2.empno"
					//+ " where s.stateno = 0"
					+ " order by applyno asc";
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				EapplyList eapplyList = new EapplyList();
				
				eapplyList.setApplyno(rs.getInt(1));
				eapplyList.setStateno(rs.getInt(2));
				eapplyList.setEmpno(rs.getInt(3));
				eapplyList.setVacationDays(rs.getString(4));
				eapplyList.setHname(rs.getString(5));
				eapplyList.setReason(rs.getString(6));
				eapplyList.setSinfo(rs.getString(7));
				eapplyList.setRestday(rs.getString(8));
				elLists.put(rs.getInt(1), eapplyList);
			}
	
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}finally {
			SingletonHelper.close(rs);
			SingletonHelper.close(pstmt);
		}
		return elLists;
	}
	
	//휴가 신청
	public void insertVacationRequest(int holidayno, Date start_date, Date end_date, String reason) {
		try {
			conn = SingletonHelper.getConnection("oracle");
			String sql = "insert into eapply values(apl_seq.nextval, ?, ?, 0, ?, ?, ?)";
			pstmt = conn.prepareStatement(sql);
			

			pstmt.setInt(1, emp.getEmpno());
			pstmt.setInt(2, holidayno);
			pstmt.setDate(3, start_date);
			pstmt.setDate(4, end_date);
			pstmt.setString(5, reason);
			
			
			 pstmt.executeUpdate();
	
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}finally {
			SingletonHelper.close(rs);
			SingletonHelper.close(pstmt);
		}
	}
	
	//휴가 승인
	public void okayVacation(int stateno, int applyno) {
		try {
			conn = SingletonHelper.getConnection("oracle");
			String sql = "update eapply set stateno = ? where applyno = ?";
			pstmt = conn.prepareStatement(sql);
			

			pstmt.setInt(1, stateno);
			pstmt.setInt(2, applyno);
		
			
	
			 pstmt.executeUpdate();
	
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}finally {
			SingletonHelper.close(pstmt);
		}
		
	}
	//휴가 차감
	public void minusVacation(int empno, int applyno) {
		
		int rest_Vacation = 0;
		int minus_Vacation = 0;
		int now_Vacation = 0;
		PreparedStatement pstmt2 = null;
		PreparedStatement pstmt3 = null;
		try {
			conn = SingletonHelper.getConnection("oracle");
			String sql = "select r.restday from rest_holiday r join emp e on e.empno = r.empno where r.empno = ? ";
			String sql2 = "select ((to_char(end_date, 'FMDD')-to_char(start_date,'FMDD'))+1) as 신청일수 "
					+ " from eapply where applyno = ?";
			String sql3 = "update rest_holiday set restday = ?  where empno = ?";
			pstmt = conn.prepareStatement(sql);
		
			pstmt.setInt(1, empno);
			
			rs = pstmt.executeQuery();
		
			if(rs.next()) {
				rest_Vacation = rs.getInt(1);
			}
		
			pstmt2 = conn.prepareStatement(sql2);
			
			pstmt2.setInt(1, applyno);
			rs = pstmt2.executeQuery();
			
			if(rs.next()) {
				minus_Vacation = rs.getInt(1);
			}
			
			
			
			if(rest_Vacation > minus_Vacation) {
				now_Vacation = rest_Vacation - minus_Vacation;
			
				pstmt3 = conn.prepareStatement(sql3);
				
				pstmt3.setInt(1, now_Vacation);
				pstmt3.setInt(2, empno);
			
				pstmt3.executeUpdate();
			}else {
				System.out.println("사용하시려는 휴가일수가 잔여일보다 많습니다.");
			}
	
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}finally {
			SingletonHelper.close(rs);
			SingletonHelper.close(pstmt);
		}
	}
	
	//내 잔여 휴가일수
	public int myVacation() {
		int my_Vacation = 0;
		try {
			conn = SingletonHelper.getConnection("oracle");
			String sql = "select r.restday from rest_holiday r join emp e on e.empno = r.empno where r.empno = ? ";
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setInt(1, emp.getEmpno());
			
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				my_Vacation = rs.getInt(1);
			}
			
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}finally {
			SingletonHelper.close(rs);
			SingletonHelper.close(pstmt);
		}
		return my_Vacation;
	}
	
	//특정 신청목록 삭제
	public void delectApply(int applyno){
		try {
			conn = SingletonHelper.getConnection("oracle");
			String sql = "delete from eapply where applyno =  ?";
			pstmt = conn.prepareStatement(sql);
			
			pstmt.setInt(1, applyno);
			
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				Emp emp = new Emp();
			
				emp.setEmpno(rs.getInt(1));
				emp.setRankno(rs.getInt(2));
				emp.setDeptno(rs.getInt(3));
				emp.setMgr(rs.getInt(4));
				emp.setEname(rs.getString(5));
				emp.setId_number(rs.getString(6));
				emp.setAge(rs.getInt(7));
				emp.setTel(rs.getString(8));
				emp.setHiredate(dateFormat.format(rs.getDate(9)));
				emp.setEmail(rs.getString(10));
				emp.setAddr(rs.getString(11));
	
			}
	
		} catch (Exception e) {
			//System.out.println(e.getMessage());
		}finally {
			SingletonHelper.close(rs);
			SingletonHelper.close(pstmt);
		}
		
	}
	

	//로그인 창
	public void loginPrint() {
		
		int empno = 0;
		String id_number =null;
		System.out.println("[로그인 해주세요]");
		while(true) {
			  try {
					System.out.println("사원번호 입력");
					empno = Integer.parseInt(sc.nextLine());
					System.out.println("주민번호 입력(- 빼고 13자리 입력)");
					id_number =sc.nextLine();
						if(logIn(empno,  id_number) == true) {
							System.out.println(emp.getEname()+"님 로그인 하셨습니다");
							run();
							break;
						}
						else if (logIn(empno,  id_number) == false) {
							System.out.println("등록되지 않는 사원번호와 주민번호입니다.");
						}
				} catch (Exception e) {
					System.out.println("형식에 맞게 입력해주세요");
					}	
				}
		
	}
	
	public void run () {
		int lv_num=0;
		Map<Integer, EapplyList>  elist = vacationRequestList();
		
		while(true) {
			System.out.println("1. 사용가능휴가일수 2. 휴가신청 3. 신청현황 4. 휴가승인 5.신청목록삭제 6. 로그아웃");
			lv_num = Integer.parseInt(sc.nextLine());
			switch (lv_num) {
			case 1:{
				if(emp != null) {
					if(myVacation() > 0) {
						System.out.println(emp.getEname()+"님의 남은 휴가 일수:"+myVacation());
					}
				}else {
					System.out.println("로그인 먼저해주세요");
				}
				break;
			}
			case 2:{
			while(true) {
					try {
						if(emp != null) {
							if(myVacation() > 0) {
							
									System.out.println("휴가 종류를 선택해주세요");
									System.out.println("1.공가 2. 병가 3. 경소사");
									int holidayno = Integer.parseInt(sc.nextLine());
									System.out.println("휴가 시작일 입력");
									String start_day = sc.nextLine();
									System.out.println("휴가 종료일 입력");
									String end_day = sc.nextLine();
									System.out.println("사유 입력");
									String reason = sc.nextLine();
									
									insertVacationRequest(holidayno, date(start_day), date(end_day), reason);
									System.out.println("신청완료 되었습니다.");
								} else 
									System.out.println(emp.getEname()+"님의 남은 휴가 일수가 없습니다");
								}else {
									System.out.println("로그인부터 해주세요");
							
								}
					} catch (Exception e) {
						System.out.println();
					}
					break;
			}
				break;
			}
			case 3 : {
				elist = vacationRequestList();
			
				for (Integer key : elist.keySet()) {
				    EapplyList value = elist.get(key);
				    System.out.println(value);
				}
				break;
			}
			case 4 :{
				try {
					while(true) {
						if(emp.getEmpno()  == 1) {
							System.out.println("어떤 안건부터 처리하시겠습니까?(applyno)");
							int applyno =  Integer.parseInt(sc.nextLine());
									if(elist.get(applyno).getApplyno() == applyno) {
											System.out.println("1. 승인처리 2. 반려처리");
											int stateno = Integer.parseInt(sc.nextLine());
											System.out.println(elist.get(applyno).getApplyno());
											System.out.println(elist.get(applyno).getStateno());
											if(elist.get(applyno).getStateno() ==  0 || elist.get(applyno).getStateno() ==  2) {
												
												okayVacation(stateno, applyno); 
												minusVacation(elist.get(applyno).getEmpno() ,elist.get(applyno).getApplyno());
												}else{
													System.out.println(" 이미 처리된 안건입니다. ");
												}
											}else {
											System.out.println("해당 안건은 목록에 존재하지 않습니다.");
										}
				
						}else {
							System.out.println("당신은 관리자가 아닙니다. 다른 계정으로 로그인 하세요");
							}
							break;
					}
				} catch (Exception e) {
					System.out.println("입력문자를 틀리셨습니다.");
					
				}
				break;
			}
			case 5:{
				while(true) {
					try {
						if(emp.getEmpno()  == 1) {
						System.out.println("삭제하실 안건을 입력해주세요(applyno)");
						int applyno =  Integer.parseInt(sc.nextLine());
						delectApply(applyno);
						}else {
							System.out.println("당신은 관리자가 아닙니다. 다른 계정으로 로그인 하세요");
						}
					} catch (Exception e) {
						System.out.println("잘못 입력하셔씁니다.");
					}
					break;
				}
				break;
			}
			case 6: {
				logOut();
				loginPrint();
				break;
			}
			
			default:
				System.out.println("잘못 입력하셨습니다.");
			
			}
		}
	}

public static java.sql.Date date(String str){
		
	 	SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
		java.util.Date date = null;
		java.sql.Date sdate = null;
		
		try {
			date = dateFormat2.parse(str);
			sdate = new java.sql.Date(date.getTime());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
		
		return sdate;
		
	}
}
