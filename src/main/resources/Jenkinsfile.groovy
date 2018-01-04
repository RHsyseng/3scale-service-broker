#!/usr/bin/groovy

node { 

    def accessToken = "55044249b6efeaa6ff383df3ac3709824ba51f79438ef5aa57b134e381120c78"
    def ampURL = ""
    def serviceCurl = ""

    stage ('clean services') {
        // Git checkout before load source the file
        checkout scm
        // To know files are checked out or not
        sh "ls -lhrt"
        def rootDir = pwd()
        println("Current Directory: " + rootDir)

        ampURL = "\"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services.xml\""
        serviceCurl = "curl -v -k -X GET -d \"access_token=" + accessToken + "\" " + ampURL + " >out_listService.txt"
        //echo " serviceCurl: ${serviceCurl}"
        sh "${serviceCurl}"
        
        def listServiceReply = new File("${WORKSPACE}/out_listService.txt").text
        echo "listServiceReply: ${listServiceReply}"  
        

        def ReadIdHelper = load("ReadIdHelper.groovy")
        
        //just get 1 service id
        def serviceId = Integer.parseInt(ReadIdHelper.getServiceId2(listServiceReply))
        echo "serviceId ${serviceId}"
        
        if (serviceId > 0)
        {
            //use for loop clean the rest 10 service, should be enough for the lab env
            for(int i = serviceId; i < serviceId + 10; ++ i) {
                echo "here ${i}"
                ampURL = "\"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services/" + i + ".xml\""
                serviceCurl = "curl -v -k -X DELETE -d \"access_token=" + accessToken + "\" " + ampURL + " >>out_deleteService.txt"
                //echo "serviceCurl: ${serviceCurl}"   
                sh "${serviceCurl}"
            }     
        }else{
            println("no need to clean service, only 1 left")
        }    
        
    }
    
    
    stage ('create service1') {
        println("create service ----------------------------------")
        def serviceName = "printPhoto"
        ampURL = "\"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services.xml\""
        serviceCurl = "curl -v -k -X POST -d \"access_token=" + accessToken + "&name=" + serviceName + "\" " + ampURL + " >out_createService.txt"
        //echo " serviceCurl: ${serviceCurl}"
        sh "${serviceCurl}"

        //create application plan
        println("create application plan  ----------------------------------")
        def createServiceReply = new File("${WORKSPACE}/out_createService.txt").text
        echo "createServiceReply: ${createServiceReply}"

        def ReadIdHelper = load("ReadIdHelper.groovy")
        def serviceId = ReadIdHelper.getServiceId(createServiceReply)
        echo "serviceId ${serviceId}"

        def planName = "fourInchPlan"
        serviceCurl = "curl -v -k -X POST -d \"access_token=" + accessToken + "&name=" + planName + "\" \"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services/" + serviceId + "/application_plans.xml\"  > out_createApplicationPlan.txt "
        //echo " serviceCurl: ${serviceCurl}"
        sh "${serviceCurl}"      

        
        planName = "tenInchPlan"
        serviceCurl = "curl -v -k -X POST -d \"access_token=" + accessToken + "&name=" + planName + "\" \"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services/" + serviceId + "/application_plans.xml\"  > out_createApplicationPlan.txt "
        //echo " serviceCurl: ${serviceCurl}"
        sh "${serviceCurl}"   
        
       
        def createPlanReply = new File("${WORKSPACE}/out_createApplicationPlan.txt").text
        def planId = ReadIdHelper.getPlanId(createPlanReply)
        echo "planId ${planId}"
        
        //API integration
        println("API integration  ----------------------------------")
        ampURL = "\"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services/" + serviceId + "/proxy.xml\""
        serviceCurl = "curl -v -k -X PATCH -d \"access_token=" + accessToken + "&api_backend=https%3A%2F%2Fgoogle.com\" " + ampURL + " >out_integration.txt"
        echo " serviceCurl: ${serviceCurl}"
        sh "${serviceCurl}"  
        
        //create application
        println("create application  ----------------------------------")
        def applicationName = "printPhotoApp"
        ampURL = "\"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/accounts/4/applications.xml\""
        serviceCurl = "curl -v -k -X POST -d \"access_token=" + accessToken + "&name=" + applicationName + "&plan_id=" + planId + "&description=" + applicationName + "\" " + ampURL + " >out_integration.txt"
        echo " serviceCurl: ${serviceCurl}"
        sh "${serviceCurl}"  
     
    }
    
    stage ('create service2') {
        println("create service ----------------------------------")
        def serviceName = "buyTicket"
        ampURL = "\"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services.xml\""
        serviceCurl = "curl -v -k -X POST -d \"access_token=" + accessToken + "&name=" + serviceName + "\" " + ampURL + " >out_createService.txt"
        //echo " serviceCurl: ${serviceCurl}"
        sh "${serviceCurl}"

        //create application plan
        println("create application plan  ----------------------------------")
        def createServiceReply = new File("${WORKSPACE}/out_createService.txt").text
        echo "createServiceReply: ${createServiceReply}"

        def ReadIdHelper = load("ReadIdHelper.groovy")
        def serviceId = ReadIdHelper.getServiceId(createServiceReply)
        echo "serviceId ${serviceId}"

        def planName = "seasonTicketPlan"
        serviceCurl = "curl -v -k -X POST -d \"access_token=" + accessToken + "&name=" + planName + "\" \"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services/" + serviceId + "/application_plans.xml\"  > out_createApplicationPlan.txt "
        //echo " serviceCurl: ${serviceCurl}"
        sh "${serviceCurl}"      

        def createPlanReply = new File("${WORKSPACE}/out_createApplicationPlan.txt").text
        def planId = ReadIdHelper.getPlanId(createPlanReply)
        echo "planId ${planId}"
        
        //API integration
        println("API integration  ----------------------------------")
        ampURL = "\"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services/" + serviceId + "/proxy.xml\""
        serviceCurl = "curl -v -k -X PATCH -d \"access_token=" + accessToken + "&api_backend=https%3A%2F%2Fgoogle.com\" " + ampURL + " >out_integration.txt"
        echo " serviceCurl: ${serviceCurl}"
        sh "${serviceCurl}"  
        
        //create application
        println("create application  ----------------------------------")
        def applicationName = "buyTicketApp"
        ampURL = "\"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/accounts/4/applications.xml\""
        serviceCurl = "curl -v -k -X POST -d \"access_token=" + accessToken + "&name=" + applicationName + "&plan_id=" + planId + "&description=" + applicationName + "\" " + ampURL + " >out_integration.txt"
        echo " serviceCurl: ${serviceCurl}"
        sh "${serviceCurl}"  
     
    }
    
    stage ('create service3') {
        println("create service ----------------------------------")
        def serviceName = "orderPizza"
        ampURL = "\"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services.xml\""
        serviceCurl = "curl -v -k -X POST -d \"access_token=" + accessToken + "&name=" + serviceName + "\" " + ampURL + " >out_createService.txt"
        //echo " serviceCurl: ${serviceCurl}"
        sh "${serviceCurl}"

        //create application plan
        println("create application plan  ----------------------------------")
        def createServiceReply = new File("${WORKSPACE}/out_createService.txt").text
        echo "createServiceReply: ${createServiceReply}"

        def ReadIdHelper = load("ReadIdHelper.groovy")
        def serviceId = ReadIdHelper.getServiceId(createServiceReply)
        echo "serviceId ${serviceId}"

        def planName = "smallPizza"
        serviceCurl = "curl -v -k -X POST -d \"access_token=" + accessToken + "&name=" + planName + "\" \"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services/" + serviceId + "/application_plans.xml\"  > out_createApplicationPlan.txt "
        //echo " serviceCurl: ${serviceCurl}"
        sh "${serviceCurl}"      

        def createPlanReply = new File("${WORKSPACE}/out_createApplicationPlan.txt").text
        def planId = ReadIdHelper.getPlanId(createPlanReply)
        echo "planId ${planId}"
        
        //API integration
        println("API integration  ----------------------------------")
        ampURL = "\"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/services/" + serviceId + "/proxy.xml\""
        serviceCurl = "curl -v -k -X PATCH -d \"access_token=" + accessToken + "&api_backend=https%3A%2F%2Fgoogle.com\" " + ampURL + " >out_integration.txt"
        echo " serviceCurl: ${serviceCurl}"
        sh "${serviceCurl}"  
        
        //create application
        println("create application  ----------------------------------")
        def applicationName = "orderPizzaApp"
        ampURL = "\"https://3scale-admin.middleware.ocp.cloud.lab.eng.bos.redhat.com/admin/api/accounts/4/applications.xml\""
        serviceCurl = "curl -v -k -X POST -d \"access_token=" + accessToken + "&name=" + applicationName + "&plan_id=" + planId + "&description=" + applicationName + "\" " + ampURL + " >out_integration.txt"
        echo " serviceCurl: ${serviceCurl}"
        sh "${serviceCurl}"  
     
    }
    

}
