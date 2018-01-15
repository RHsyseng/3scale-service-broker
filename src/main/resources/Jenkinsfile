#!/usr/bin/groovy

node { 

    def accessToken = "55044249b6efeaa6ff383df3ac3709824ba51f79438ef5aa57b134e381120c78"
    def ampURL = ""
    def serviceCurl = ""
    def OC_HOME = "/home/czhu/works/ocClient"    
    
    
    stage ('Clean 3scale services') {
        println("------------------------------------------------------- Clean 3scale services  -------------------------------------------------------")
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
        

        def ReadIdHelper = load("src/main/resources/ReadIdHelper.groovy")
        
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
        println("------------------------------------------------------- Clean 3scale services is finished -------------------------------------------------------")
        
    }
    
    
    stage ('create 3scale service1') {
        println("------------------------------------------------------- create 3scale service1  -------------------------------------------------------")
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

        def ReadIdHelper = load("src/main/resources/ReadIdHelper.groovy")
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
        println("------------------------------------------------------- create 3scale service1 is finished -------------------------------------------------------")
     
    }
    
    stage ('create 3scale service2') {
        println("------------------------------------------------------- create 3scale service2  -------------------------------------------------------")
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

        def ReadIdHelper = load("src/main/resources/ReadIdHelper.groovy")
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
        println("------------------------------------------------------- create 3scale service2 is finished -------------------------------------------------------")
     
    }
    
    stage ('create 3scale service3') {
        println("------------------------------------------------------- create 3scale service3  -------------------------------------------------------")
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

        def ReadIdHelper = load("src/main/resources/ReadIdHelper.groovy")
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
        println("------------------------------------------------------- create 3scale service3 is finished -------------------------------------------------------")
     
    }
    
    
    stage ('OCP Build and Deploy') {
        println("------------------------------------------------------- OCP Build and Deploy  -------------------------------------------------------")
        
        //delete the old three-scale application first
        withEnv(["PATH+OC=${OC_HOME}"]) {
            sh "${OC_HOME}/oc delete all -l app=three-scale  --grace-period=0"
        }

        //deploy the new code
        withMaven(
            // Maven installation declared in the Jenkins "Global Tool Configuration"
            maven: 'maven 3.3.3',
            // Maven settings.xml file defined with the Jenkins Config File Provider Plugin
            // Maven settings and global settings can also be defined in Jenkins Global Tools Configuration
            //mavenSettingsConfig: 'my-maven-settings',
            mavenLocalRepo: '.repository') {

            // Run the maven build
            sh "mvn clean fabric8:deploy -Popenshift"

        } // withMaven will discover the generated Maven artifacts, JUnit Surefire & FailSafe & FindBugs reports...
        
        println("------------------------------------------------------- OCP Build and Deploy is finished -------------------------------------------------------")
        
    }
    
   
    stage ('Recreate Broker') {
        //API integration
        println("------------------------------------------------------- Recreate Broker  -------------------------------------------------------")
        //delete the old three-scale application first
        withEnv(["PATH+OC=${OC_HOME}"]) {
            sh "${OC_HOME}/oc delete ClusterServiceBroker 3scale-broker"
            sh "sleep 5"
            sh "${OC_HOME}/oc get ClusterServiceBroker"
                    
            sh "${OC_HOME}/oc create -f 3scale-broker.yml"
            sh "sleep 5"
            sh "${OC_HOME}/oc describe ClusterServiceBroker 3scale-broker"
        }        

        println("------------------------------------------------------- Recreate Broker is finished -------------------------------------------------------")
    }    
}
