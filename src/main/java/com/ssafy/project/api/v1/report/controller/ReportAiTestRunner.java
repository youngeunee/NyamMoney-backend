//package com.ssafy.project.api.v1.report.controller;
//
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.ssafy.project.api.v1.openai.service.ReportAiService;
//import com.ssafy.project.api.v1.report.dto.PersonaResult;
//
//import lombok.RequiredArgsConstructor;
//
//@RestController
//@RequestMapping("/api/v1/test/report")
//@Component
//@RequiredArgsConstructor
//public class ReportAiTestRunner implements CommandLineRunner {
//
//    private final ReportAiService reportAiService;
//
//    @Override
//    public void run(String... args) {
//
//    	String result = reportAiService.summarizeMonthly(
//    			2025,
//    			11,
//    			500000,
//    			250000,
//    			20.0,
//    			"카페/디저트"
//    			);  
//    	String emotion = reportAiService.summarizeEmotionConsumption(
//    			500000,
//    			250000,
//    			20.3,
//    			"카페/디저트"
//    			);
//    	PersonaResult persona = reportAiService.summarizeSpendingPersona(
//    			580000,
//    			120000,
//    			20.7,
//    			"카페/디저트"
//    			);
//        
//        
//        System.out.println("AI SUMMARY = " + result);
//        System.out.println("EMOTION SUMMARY = " + emotion);
//        
//        System.out.println("PERSONA = " + persona.getSpendingPersona());
//        System.out.println("REASON  = " + persona.getPersonaReason());
//    }
//    
//
//}
