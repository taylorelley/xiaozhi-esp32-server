# Alibaba Cloud SMS Integration Guide

Log in to the Alibaba Cloud console and go to the "SMS Service" page: https://dysms.console.aliyun.com/overview

## Step 1: Add a signature
![Step](images/alisms/sms-01.png)
![Step](images/alisms/sms-02.png)

After completing the steps above, you will obtain a signature. Please enter it into the control console parameter `aliyun.sms.sign_name`.

## Step 2: Add a template
![Step](images/alisms/sms-11.png)

After completing the steps above, you will obtain a template code. Please enter it into the control console parameter `aliyun.sms.sms_code_template_code`.

Note: the signature must wait up to 7 working days for carrier approval before SMS can be sent successfully.

Note: the signature must wait up to 7 working days for carrier approval before SMS can be sent successfully.

Note: the signature must wait up to 7 working days for carrier approval before SMS can be sent successfully.

You can continue with the following steps after approval.

## Step 3: Create an SMS account and enable permissions

Log in to the Alibaba Cloud console and go to the "RAM" (Resource Access Management) page: https://ram.console.aliyun.com/overview?activeTab=overview

![Step](images/alisms/sms-21.png)
![Step](images/alisms/sms-22.png)
![Step](images/alisms/sms-23.png)
![Step](images/alisms/sms-24.png)
![Step](images/alisms/sms-25.png)

After completing the steps above, you will obtain an access_key_id and access_key_secret. Please enter them into the control console parameters `aliyun.sms.access_key_id` and `aliyun.sms.access_key_secret`.
## Step 4: Enable phone registration

1. Normally, after filling in all of the above information, you should see the following result. If not, you may have missed a step.

![Step](images/alisms/sms-31.png)

2. Enable allowing non-admin users to register, by setting the parameter `server.allow_user_register` to `true`.

3. Enable the phone-registration feature, by setting the parameter `server.enable_mobile_register` to `true`.
![Step](images/alisms/sms-32.png)
