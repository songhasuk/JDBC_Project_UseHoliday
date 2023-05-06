package service;

import java.util.Map;
import java.util.Scanner;

import dao.UseHoliday;
import dto.EapplyList;
import dto.Emp;

public class UseHolidayPrinter {
	Scanner sc = new Scanner(System.in);
	UseHoliday us = new UseHoliday();
	Emp emp;
	
	//로그아웃
	public void logOut() {
		if(emp != null) {
		emp = null;
		System.out.println("로그아웃 되었습니다.");
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
					if(us.logIn(empno,  id_number) == true) {
						System.out.println(emp.getEname()+"님 로그인 하셨습니다");
						run();
						break;
					}
					else if (us.logIn(empno,  id_number) == false) {
						System.out.println("등록되지 않는 사원번호와 주민번호입니다.");
					}
			} catch (Exception e) {
				System.out.println("형식에 맞게 입력해주세요");
				}	
			}
			
	}
		
	public void run () {
		int lv_num=0;
		Map<Integer, EapplyList>  elist = us.vacationRequestList();
			
		while(true) {
			System.out.println("1. 사용가능휴가일수 2. 휴가신청 3. 신청현황 4. 휴가승인 5.신청목록삭제 6. 로그아웃");
			lv_num = Integer.parseInt(sc.nextLine());
			switch (lv_num) {
			case 1:{
				if(emp != null) {
					if(us.myVacation() > 0) {
						System.out.println(emp.getEname()+"님의 남은 휴가 일수:"+us.myVacation());
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
							if(us.myVacation() > 0) {
								
									System.out.println("휴가 종류를 선택해주세요");
									System.out.println("1.공가 2. 병가 3. 경소사");
									int holidayno = Integer.parseInt(sc.nextLine());
									System.out.println("휴가 시작일 입력");
									String start_day = sc.nextLine();
									System.out.println("휴가 종료일 입력");
									String end_day = sc.nextLine();
									System.out.println("사유 입력");
									String reason = sc.nextLine();
									
									us.insertVacationRequest(holidayno, us.date(start_day), us.date(end_day), reason);
									System.out.println("신청완료 되었습니다.");									} else 
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
				elist = us.vacationRequestList();
				
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
												
												us.okayVacation(stateno, applyno); 
												us.minusVacation(elist.get(applyno).getEmpno() ,elist.get(applyno).getApplyno());
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
						us.delectApply(applyno);
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
	
}
