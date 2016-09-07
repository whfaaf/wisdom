package com.wisdom.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wisdom.bean.PMajor;
import com.wisdom.bean.PScore;
import com.wisdom.bean.PStudentStatus;
import com.wisdom.bean.UMajor;
import com.wisdom.bean.UScore;
import com.wisdom.bean.UStudentStatus;
import com.wisdom.entity.Label;
import com.wisdom.entity.User;
import com.wisdom.service.UScoreAnalysisService;
import com.wisdom.service.UserService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

//webApp 成绩分析模块
@Controller
@RequestMapping(value="/scoreAnalysis")
public class ScoreAnalysisController extends BaseController{
	@Autowired
	private UScoreAnalysisService uscoreAnalysisService;
	@Autowired
	private UserService userService;

	//成绩概况
	@ResponseBody
	@RequestMapping(value = "/scoreSurvey")
	public JSONObject scoreSurvey(HttpServletResponse res,
			@RequestBody JSONObject json) {
		
		String code =json.getString("code");
		String account = json.getString("account");
		String shoolyear = json.getString("schoolyear");
		String term = json.getString("term");
		json.clear();
		
		json=checkUser(account,code);
		if( json.get("result").equals("checked")){
			User u = userService.findUserByAccount(account);
			Set<Label> ul = u.getLabels();
			boolean sr = true;
			for(Label l:ul){
				if(l.getLabelName().equals("研究生")){
					sr = false;
				}
			}
			if(sr){
				List<UScore> uscores = new ArrayList<>();
				if(term.equals("0")){
					uscores = uscoreAnalysisService.findUscoresByYear(account, shoolyear);
				}
				else{
					uscores = uscoreAnalysisService.findUscoresByYearAndTerm(account, shoolyear, term);
				}
				int count_60=0,count60=0,count70=0,count80=0,count90=0;
				for(UScore us:uscores){
					float sc = us.getConvertedScore();
					int rec = (int) (sc/10);
					switch(rec){
					case 0:
					case 1:
					case 2:
					case 3:
					case 4:
					case 5:
						++count_60;break;
					case 6:
						++count60;break;
					case 7:
						++count70;break;
					case 8:
						++count80;break;
					case 9:
					case 10:
						++count90;break;
					default:break;
					}
				}
				json.put("account_60", count_60);
				json.put("account60", count60);
				json.put("account70", count70);
				json.put("account80", count80);
				json.put("account90", count90);
				json.put("result", "success");
			}
			//研究生
			else{
				List<PScore> pscores = new ArrayList<>();
				if(shoolyear.equals("0")){
					pscores = uscoreAnalysisService.findPscoresByAccount(account);
				}
				else{
					pscores = uscoreAnalysisService.findPscoresByYear(account, shoolyear);
				}
				int count_60=0,count60=0,count70=0,count80=0,count90=0;
				for(PScore ps:pscores){
					double sc = ps.getCourseScore();
					int rec = (int) (sc/10);
					switch(rec){
					case 0:
					case 1:
					case 2:
					case 3:
					case 4:
					case 5:
						++count_60;break;
					case 6:
						++count60;break;
					case 7:
						++count70;break;
					case 8:
						++count80;break;
					case 9:
					case 10:
						++count90;break;
					default:break;
					}
				}
				json.put("account_60", count_60);
				json.put("account60", count60);
				json.put("account70", count70);
				json.put("account80", count80);
				json.put("account90", count90);
				json.put("result", "success");
			}
		}
		
		return json;
		
	}
	
	//成绩波动
	@ResponseBody
	@RequestMapping(value = "/scoreWave")
	public JSONObject scoreWave(HttpServletResponse res,
			@RequestBody JSONObject json) {
		
		String code =json.getString("code");
		String account = json.getString("account");
		int rec = json.getInt("switch");
		json.clear();
		
		json=checkUser(account,code);
		if( json.get("result").equals("checked")){
			User u = userService.findUserByAccount(account);
			Set<Label> ul = u.getLabels();
			boolean sr = true;
			for(Label l:ul){
				if(l.getLabelName().equals("研究生")){
					sr = false;
				}
			}
			JSONObject userjson = new JSONObject();
			if(sr){
				JSONArray array = new JSONArray();
				UStudentStatus ustusta = uscoreAnalysisService.findUStudentStatusByAccount(account);
				String year = ustusta.getGrade();
				int y = Integer.parseInt(year);
				for(int i=0;i<4;i++){
					for(int k=1;k<=2;k++){
						String term = String.valueOf(k);
						String schoolyear = String.valueOf(y)+"-"+String.valueOf(y+1);
						List<UScore> uscores = uscoreAnalysisService.findUscoresByYearAndTerm(account, schoolyear, term);
						if(uscores!=null){
							switch(rec){
							case 0:
								userjson = doAvg(uscores);
								array.add(userjson);
								userjson.clear();
								break;
							case 1:
								userjson = doMax(uscores);
								array.add(userjson);
								userjson.clear();
								break;
							case 2:
								userjson = doMin(uscores);
								array.add(userjson);
								userjson.clear();
								break;
							default:break;
							}
							
						}
						
					}
					++y;
				}
				
				userjson.put("count", array);
				userjson.put("result", "success");
				
			}
			//研究生
			else{
				JSONArray array = new JSONArray();
				PStudentStatus pstusta = uscoreAnalysisService.findPStudentStatusByAccount(account);
				String year = pstusta.getGrade();
				int y = Integer.parseInt(year);
				for(int i=0;i<4;i++){
					for(int k=1;k<=2;k++){
						String term = String.valueOf(k);
						String schoolyear = String.valueOf(y)+"-"+String.valueOf(y+1);
						List<PScore> pscores = uscoreAnalysisService.findPscoresByYearAndTerm(account, schoolyear, term);
						if(pscores!=null){
							switch(rec){
							case 0:
								userjson = doAvgP(pscores);
								array.add(userjson);
								userjson.clear();
								break;
							case 1:
								userjson = doMaxP(pscores);
								array.add(userjson);
								userjson.clear();
								break;
							case 2:
								userjson = doMinP(pscores);
								array.add(userjson);
								userjson.clear();
								break;
							default:break;
							}
							
						}
						
					}
					++y;
				}
				
				userjson.put("count", array);
				userjson.put("result", "success");
				
			
			}
			return userjson;
		}
		
		return json;
	}
	
	private JSONObject doAvg(List<UScore> uscores){
		JSONObject json = new JSONObject();
		double sum = 1.0f;
		for(UScore us:uscores){
			sum += us.getConvertedScore();
		}
		double avg = sum/uscores.size();
		json.put("num", avg);
		return json;
	}
	
	private JSONObject doMax(List<UScore> uscores){
		JSONObject json = new JSONObject();
		double max = 0.0f;
		for(UScore us:uscores){
			double f = us.getConvertedScore();
			if(f>max){
				max = f;
			}
		}
		json.put("num", max);
		return json;
	}
	
	private JSONObject doMin(List<UScore> uscores){
		JSONObject json = new JSONObject();
		double min = 0.0f;
		for(UScore us:uscores){
			double f = us.getConvertedScore();
			if(f<min){
				min = f;
			}
		}
		json.put("num", min);
		return json;
	}
	
	private JSONObject doAvgP(List<PScore> pscores){
		JSONObject json = new JSONObject();
		double sum = 1.0;
		for(PScore ps:pscores){
			sum += ps.getCourseScore();
		}
		double avg = sum/pscores.size();
		json.put("num", avg);
		return json;
	}
	
	private JSONObject doMaxP(List<PScore> pscores){
		JSONObject json = new JSONObject();
		double max = 0.0;
		for(PScore ps:pscores){
			double f = ps.getCourseScore();
			if(f>max){
				max = f;
			}
		}
		json.put("num", max);
		return json;
	}
	
	private JSONObject doMinP(List<PScore> pscores){
		JSONObject json = new JSONObject();
		double min = 0.0;
		for(PScore ps:pscores){
			double f = ps.getCourseScore();
			if(f<min){
				min = f;
			}
		}
		json.put("num", min);
		return json;
	}
	
	//学科成绩分析
	@ResponseBody
	@RequestMapping(value = "/courseSurvey")
	public JSONObject courseSurvey(HttpServletResponse res,
			@RequestBody JSONObject json) {
		
		String code =json.getString("code");
		String account = json.getString("account");
		String schoolyear = json.getString("schoolyear");
		String term = json.getString("term");
		
		json.clear();
		
		json=checkUser(account,code);
		if( json.get("result").equals("checked")){
			User u = userService.findUserByAccount(account);
			Set<Label> ul = u.getLabels();
			boolean sr = true;
			for(Label l:ul){
				if(l.getLabelName().equals("研究生")){
					sr = false;
				}
			}
			JSONObject userjson = new JSONObject();
			if(sr){
				JSONArray array = new JSONArray();
				List<UScore> uscores = uscoreAnalysisService.findUscoresByYearAndTerm(account, schoolyear, term);
				UScoreComparator usc = new UScoreComparator();
				uscores.sort(usc);
				for(int i=0;i<3;i++){
					userjson.put("score", uscores.get(i).getConvertedScore());
					userjson.put("courseName", uscores.get(i).getCourse().getName());
					array.add(userjson);
					userjson.clear();
				}
				for(int i=uscores.size()-1;i>=uscores.size()-3;i--){
					userjson.put("score", uscores.get(i).getConvertedScore());
					userjson.put("courseName", uscores.get(i).getCourse().getName());
					array.add(userjson);
					userjson.clear();
				}
				userjson.put("count", array);
				userjson.put("result", "success");
				return userjson;
				
			}
			//研究生
			else{
				JSONArray array = new JSONArray();
				List<PScore> pscores = uscoreAnalysisService.findPscoresByYearAndTerm(account, schoolyear, term);
				PScoreComparator psc = new PScoreComparator();
				pscores.sort(psc);
				for(int i=0;i<3;i++){
					userjson.put("score", pscores.get(i).getCourseScore());
					userjson.put("courseName", pscores.get(i).getCourse().getName());
					array.add(userjson);
					userjson.clear();
				}
				for(int i=pscores.size()-1;i>=pscores.size()-3;i--){
					userjson.put("score", pscores.get(i).getCourseScore());
					userjson.put("courseName", pscores.get(i).getCourse().getName());
					array.add(userjson);
					userjson.clear();
				}
				userjson.put("count", array);
				userjson.put("result", "success");
				return userjson;
			}
		}
		return json;
	}
	
	class UScoreComparator implements Comparator<UScore> {

		@Override
		public int compare(UScore o1, UScore o2) {
			// TODO Auto-generated method stub
			return (int) (o1.getConvertedScore()-o2.getConvertedScore());
		} 
		
	}
	
	class PScoreComparator implements Comparator<PScore> {

		@Override
		public int compare(PScore o1, PScore o2) {
			// TODO Auto-generated method stub
			return (int) (o1.getCourseScore()-o2.getCourseScore());
		} 
		
	}
	
	//女生占比和班级成绩分析
	@ResponseBody
	@RequestMapping(value = "/girlProportion")
	public JSONObject girlProportion(HttpServletResponse res,
			@RequestBody JSONObject json) {
		
//		String code =json.getString("code");
		String account = json.getString("account");
		json.clear();
		
//		json=checkUser(account,code);
		json.put("result", "checked");
		if( json.get("result").equals("checked")){
			User u = userService.findUserByAccount(account);
			Set<Label> ul = u.getLabels();
			boolean sr = true;
			for(Label l:ul){
				if(l.getLabelName().equals("研究生")){
					sr = false;
				}
			}
			if(sr){
				List<UMajor> umajors = uscoreAnalysisService.findUmajorAll();
				List<String> majs = new ArrayList<>(); //所有专业的国际专业码
				for(UMajor um:umajors){
					majs.add(um.getInternationalMajorNum());
				}
				JSONObject userjson = new JSONObject();
				JSONArray array = new JSONArray();
				for(String s:majs){
					List<UStudentStatus> ustudentStatuses = uscoreAnalysisService.findUstudentStatusByInternationalMajorNum(s);
					List<String> majStuIds = new ArrayList<>(); //某专业所有学生的学号
					for(UStudentStatus ustuSta:ustudentStatuses){
						majStuIds.add(ustuSta.getId());
					}
					
					//计算某个专业的平均分
					float total = 0.0f;
					for(String stuid:majStuIds){
						List<UScore> uscores = uscoreAnalysisService.findUscoresByAccount(stuid);
						float sum = 1.0f;
						for(UScore us:uscores){
							sum += us.getConvertedScore();
						}
						float avg = sum/uscores.size(); //某专业某学生的平均分数
						total += avg;
					}
					float majavg = total/majStuIds.size(); //某专业平均分
					
					//计算某专业的女生占比
					int count=0;
					for(String stuid:majStuIds){
						User stu = userService.findUserByAccount(stuid);
						int g = stu.getGender();
						if(g==2){
							++count;
						}
					}
					double prop = count*1.0/majStuIds.size();
					
					userjson.put("girlprop", prop);
					userjson.put("average", majavg);
					array.add(userjson);
					userjson.clear();
				}
				
				userjson.put("girlproportion", array);
				userjson.put("result", "success");
				return userjson;

			}
			//研究生
			else{
				List<PMajor> pmajors = uscoreAnalysisService.findPmajorAll();
				List<String> majs = new ArrayList<>(); //所有专业的国际专业码
				for(PMajor pm:pmajors){
					majs.add(pm.getPostgraduateMajorCode());
				}
				JSONObject userjson = new JSONObject();
				JSONArray array = new JSONArray();
				for(String s:majs){
					List<PStudentStatus> pstudentStatuses = uscoreAnalysisService.findPstudentStatusByPostgraduateMajorCode(s);
					List<String> majStuIds = new ArrayList<>(); //某专业所有学生的学号
					for(PStudentStatus pstuSta:pstudentStatuses){
						majStuIds.add(pstuSta.getId());
					}
					
					//计算某个专业的平均分
					double total = 0.0;
					for(String stuid:majStuIds){
						List<PScore> pscores = uscoreAnalysisService.findPscoresByAccount(stuid);
						double sum = 1.0;
						for(PScore ps:pscores){
							sum += ps.getCourseScore();
						}
						double avg = sum/pscores.size(); //某专业某学生的平均分数
						total += avg;
					}
					double majavg = total/majStuIds.size(); //某专业平均分
					
					//计算某专业的女生占比
					int count=0;
					for(String stuid:majStuIds){
						User stu = userService.findUserByAccount(stuid);
						int g = stu.getGender();
						if(g==2){
							++count;
						}
					}
					double prop = count*1.0/majStuIds.size();
					
					userjson.put("girlprop", prop);
					userjson.put("average", majavg);
					array.add(userjson);
					userjson.clear();
				}
				
				userjson.put("girlproportion", array);
				userjson.put("result", "success");
				return userjson;
				
			}
		}
		return json;
	}
}
