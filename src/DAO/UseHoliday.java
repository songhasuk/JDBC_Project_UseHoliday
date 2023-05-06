package dao;

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

import Utils.SingletonHelper;
import dto.Dept;
import dto.EapplyList;
import dto.Emp;
import dto.Estate;
import dto.HoliDay;
import lombok.Data;

@Data
public class UseHoliday {
	
	Connection conn = null;
	PreparedStatement pstmt = null;
	ResultSet rs = null;
	
	
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
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
