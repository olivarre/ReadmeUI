================================================================================
COMPANY-XXX PRODUCT-XXX HOTFIX README
================================================================================

Hotfix Targets:         ##Product Names and Versions##

Hotfix Release Date:    ##Hotfix Release Date##

For Company:            ##Customer Company Name##

================================================================================
HOTFIX TRACKING INFORMATION
================================================================================

Fix Request Information:

	=== HMA-##Fix Request ID## ===
	##Fix Request Title##

Problem Description(s):

##Problems Addressed By This Hotfix##

How this Hotfix addresses the problem(s) above:

##How This Hotfix Addresses the Problems##

================================================================================
HOTFIX TERMS & CONDITIONS OF USE
================================================================================

** Please read the following Hotfix disclaimer with terms and conditions 
   carefully before proceeding:
   
This hotfix <##Fix Request ID##> is being provided to you by XXX to satisfy requirements of <##Product Names and Versions##> reported in FR(s) <##Fix Request ID##>. Additional information may also be found in: PMR(s) <##PMR ID##>, APAR(s) <##APAR ID##>, or Defect(s) <##Defect ID##>. This Hotfix was developed exclusively for this purpose and has not undergone the quality assurance testing COMPANY-XXX performs on software which is released to the general public. If this proves to be a valid fix, COMPANY-XXX may incorporate the fix into a later release, at its sole discretion.

This Hotfix is specific to your configuration of <##Product Names and Versions##>, as reported in Fix Request(s) <##Fix Request ID##>. COMPANY-XXX will support this modified version of <##Product Names and Versions##> in your enterprise until this fix can be included in a production release of the code.  

In consideration of COMPANY-XXX providing you with a copy of the Hotfix at no charge, you acknowledge and agree that your use of this Hotfix is subject to the terms and conditions of the COMPANY-XXX International Program License Agreement (the IPLA) applicable to <##Product Names and Versions##>, which is incorporated herein by reference, and the following additional terms:

1.      By using this Hotfix you agree to the terms and conditions contained in the IPLA (as modified herein) and this Letter Agreement. For purposes of this Letter Agreement, all references in the IPLA to Program shall be deemed to include Hotfix.  Should there be any conflict between IPLA and this Letter Agreement with respect to this Hotfix, this Letter Agreement shall take precedence and govern.

2.      Your right to use this Hotfix is restricted. Hotfixes are intended for limited production use to address the specific issues with the specific product and configuration, as noted above, and should not otherwise be distributed throughout an organization. This Hotfix may only be used in accordance with the instructions provided by COMPANY-XXX Technical Support.

3.      This Hotfix constitutes a trade secret of COMPANY-XXX, and in that regard you may not disclose the Hotfix or any related documentation or any information concerning this Hotfix to any third party other than your employees or contractors whose use of the Hotfix is necessary solely for the resolution of a problem reported above, in accordance with the terms of this Letter Agreement.

4.      THE HOTFIX PROVIDED HAS NOT UNDERGONE TESTING AND QUALITY ASSURANCE PROCESSES. YOU SHOULD ONLY ALLOW ACCESS TO THE HOTFIX BY  KNOWLEDGEABLE AND TRAINED INDIVIDUALS. IT IS YOUR RESPONSIBILITY TO MAKE BACKUP COPIES OF ALL PROGRAMS, DATABASES AND OTHER INFORMATION BEFORE USING THE HOTFIX. COMPANY-XXX WILL NOT BE RESPONSIBLE FOR YOUR (OR YOUR CUSTOMERS') FAILURE TO DO SO, NOR FOR PROVIDING SUPPORT OR TECHNICAL ASSISTANCE IN THE EVENT OF YOUR (OR YOUR CUSTOMERS') FAILURE TO DO SO.

5.      As it pertains to the Hotfix, the following provisions shall apply:  IN LIGHT OF THE NATURE OF THE HOTFIX, THE HOTFIX IS PROVIDED AS IS, AND COMPANY-XXX DISCLAIMS ALL WARRANTIES WITH RESPECT TO THE HOTFIX AND ANY RELATED MATERIALS, INCLUDING THEIR SATISFACTORY QUALITY, PERFORMANCE, MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR ANY WARRANTY OF CONDITION OR NON-INFRINGEMENT.  COMPANY-XXX WILL HAVE NO LIABILITY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, CONSEQUENTIAL, TORT OR COVER DAMAGES ARISING OUT OF THE USE OF OR INABILITY TO USE THE HOTFIX OR RELATED MATERIALS, EVEN IF ADVISED OR AWARE OF THE POSSIBILITY OF SUCH DAMAGES. 

6.      You will, upon COMPANY-XXX's request, promptly destroy or return to COMPANY-XXX all diskettes, documentation or other materials furnished to you by COMPANY-XXX in connection with this Letter Agreement.

7.      The terms of this Letter Agreement supersede any additional or conflicting provisions in any purchase order or other written notification from your company. This is the sole and exclusive agreement between you and COMPANY-XXX with respect to the Hotfix, and you acknowledge that you have not relied on any other representations, whether written, verbal or otherwise, other than those contained herein.

COMPANY-XXX   

================================================================================
GENERAL HOTFIX INSTRUCTIONS
================================================================================

	1. With all hotfixes, make sure to back up any affected files beforehand.

	2. Stop, then Undeploy/Remove the web application (to avoid cache issues).

	3. Clear any cache or temporary files from the application server. 
       - Websphere: delete <ProfileLocation>/wstemp and <ProfileLocation>/temp
       - Weblogic: delete <WeblogicDomain>/servers/AdminServer/tmp
	
	4. Follow instructions in the "FILE MODIFICATIONS REQUIRED" section. 

	5. Follow instructions in the "ADDITIONAL PRE-DEPLOYMENT INSTRUCTIONS"
       section. 

	6. If deploying a WAR file provided by COMPANY-XXX Support, copy your original 
       <WAR file>\WEB-INF\web.xml file into the provided WAR file prior to 
	   deploying the hotfix to avoid problems with LOG4J logging.
	
	7. Redeploy the application.
	   
	8. Restart the application server. 
	
	9. Make sure to clear the web browser's cache for all users of the product.
	   Failure to clear the cache could result in the hotfix appearing to fail.

	10. Follow any additional instructions in the "ADDITIONAL POST-DEPLOYMENT 
        INSTRUCTIONS" section.

    NOTE:  APPLYING JAVA HOTFIXES WITHOUT REDEPLOYING WARS:

		When the provided hotfix is limited to a set of updated *.class files, application server administrators can consider simply updating the JAR files in the application server's decompressed WAR directory and restarting the application server. This allows the hotfixed WARs to be redeployed later at more convenient time (such as during routine maintenance windows), since deploying WARs is typically more involved than patching JARs.

================================================================================
ADDITIONAL PRE-DEPLOYMENT INSTRUCTIONS
================================================================================

##Pre-Deployment Instructions##
	
================================================================================
ADDITIONAL POST-DEPLOYMENT INSTRUCTIONS
================================================================================

##Post-Deployment Instructions##

================================================================================
FILE MODIFICATIONS REQUIRED
================================================================================

	IMPORTANT:  WAR and JAR files are a form of ZIP archive. A utility such as Java's jar command can be used to expand WAR and JAR files to a directory, replace files within them, and then recompress the directory back into an archive. As customers have experienced subtle problems performing these operations using graphical utilities, we strongly suggest using Java's jar command instead. See instructions below for using the JAR command.

	This hotfix ZIP file contains the following files, which should be placed in their corresponding paths in your install.

	----------------------------------------------------------------------------
	A.  Java classes to be replaced:
	----------------------------------------------------------------------------

##Files Included In This Hotfix##	

	----------------------------------------------------------------------------
	B.  Additional files to be replaced:
	----------------------------------------------------------------------------

##Additional Files Included In This Hotfix##

	----------------------------------------------------------------------------
	C.  Files to be manually edited:
	----------------------------------------------------------------------------

##File Modifications In This Hotfix##

	----------------------------------------------------------------------------
	D.  Instructions for "unzipping" and "patching" WAR file(s) 
	----------------------------------------------------------------------------
	*) NOTE: The following steps work for patching arbitrary WAR and JAR files.
	*) <war-file-dir> is any temp directory e.g /data/temp/ucwar

	1) Copy the WAR file to <war-file-dir>
	2) Create a new directory <war-file-dir>/unwar_dir
	3) At the command prompt, navigate to <war-file-dir>/unwar_dir
	4) Execute: 'jar -xvf ../WAR file(s)'
	5) Replace files under <war-file-dir>/unwar_dir with those from the hotfix.


	----------------------------------------------------------------------------
	E.  Instructions for "rezipping" WAR file(s) 
	----------------------------------------------------------------------------
	1) At the command prompt, navigate to <war-file-dir>
	2) Rename: <war-file-dir>/XXXX.war to XXXX.war.old
	3) Execute: 'jar -cvf WAR file(s)  ./unwar_dir/ .'
	4) <war-file-dir>/WAR file(s) is the updated war file to be deployed. 


================================================================================
DIAGNOSTIC LOGGING INSTRUCTIONS
================================================================================

	-----------------------------------------------------------
	IMPORTANT: 
	-----------------------------------------------------------

	- Hotfix README files automatically wrap at 80 characters. You MUST remove any unintended wordwrapping from the following logging configuration settings when putting them in your application's LOG4J configuration.

	- When debug settings are enabled in Product-XXXXL3.txt, the corresponding logging for that class MUST also be set to DEBUG level in your application's LOG4J configuration as described in this section. See below for these settings.

    - Logging Example: Note the required log4j.logger prefix.

		log4j.logger.com.XXXcorp.Product-XXXX.deployment.RTDeploymentManager=DEBUG

	-----------------------------------------------------------
    LOG4J Logging Requested for XXXX and XXXX UI Issues
	-----------------------------------------------------------
%% 	
	EX:    log4j.logger.com.XXXcorp.XXXX.controller.DeployCommand=DEBUG  
%%	
##XXXX Logging Lines To Add To XXXX_LOG4J.properties##

		Please enable the above debug logging for XXXXweb.log by adding the following lines to the <XXXX>/conf/XXXX_log4j.properties file: 
		
	-----------------------------------------------------------
	LOG4J Logging Requested for XXXX Server Issues
	-----------------------------------------------------------
%%
	EX:    log4j.logger.com.XXXcorp.runtime.DeploymentServlet=DEBUG
%%

##XXXX Logging Lines To Add To XXXX_LOG4J.properties##
	
		Please enable the above debug logging for XXXX.log by adding the following lines to the <XXXX>/conf/XXXX_log4j.properties file:

	-----------------------------------------------------------
	Logging Notes
	-----------------------------------------------------------
	
	* XXXX logs are collected from <XXXX>/logs/XXXXweb.log* 
	* Please include <XXXX>/version.txt when providing logs.
	
	* XXXX logs are collected from <XXXX>/logs/XXXX.log*
	* Please include <XXXX>/version.txt when providing logs.
	
	* App server configuration may relocate logs to <appserver>/<domain>/*.log

%%
================================================================================
TESTING SUMMARY
================================================================================

Testing Date:           ##Testing - Testing Date##

Testing Performed By:   
	
##Testing - Tested By##

Tests Performed:

##Testing - Tests Performed##

Testing Disclaimers:

	* N/A above indicates this is a backport of a defect that has been fixed and officially tested in a FixPack or official release.
	
	* Please see legal disclaimer above regarding hotfix testing limitations.
%%	
================================================================================
COMPANY-XXX INTERNAL SUPPORT NOTES
================================================================================

##COMPANY-XXX Internal Support Notes##