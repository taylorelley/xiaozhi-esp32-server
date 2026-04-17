# Control Console: Volcano Engine Bidirectional Streaming TTS + Voice Cloning Configuration Tutorial

This tutorial is divided into four stages: Preparation, Configuration, Cloning, and Usage. It mainly introduces how to configure Volcano Engine bidirectional streaming TTS with voice cloning via the control console.

## Stage 1: Preparation
The super administrator must first enable the Volcano Engine service in advance and obtain the App Id and Access Token. By default, Volcano Engine provides one free voice resource. This voice resource needs to be copied into this project.

If you want to clone multiple voices, you need to purchase and activate multiple voice resources. You only need to copy the voice ID (S_xxxxx) of each voice resource into this project, then assign it to the system account that will use it. Below are the detailed steps:

### 1. Enable the Volcano Engine service
Visit https://console.volcengine.com/speech/app and create an application in App Management. Check "Speech Synthesis Large Model" and "Voice Cloning Large Model".

### 2. Obtain the voice resource ID
Visit https://console.volcengine.com/speech/service/9999 and copy three pieces of information: the App Id, Access Token, and voice ID (S_xxxxx). As shown in the image:

![Obtaining voice resources](images/image-clone-integration-01.png)

## Stage 2: Configure the Volcano Engine service

### 1. Fill in the Volcano Engine configuration

Log in to the control console using the super-administrator account, click [Model Configuration] at the top, then click [Text-to-Speech] on the left side of the Model Configuration page. Find "Volcano Bidirectional Streaming TTS" (HuoshanDoubleStreamTTS), click Modify, enter your Volcano Engine `App Id` into the [Application ID] field, and enter `Access Token` into the [Access Token] field. Then save.

### 2. Assign the voice resource ID to the system account

Log in to the control console using the super-administrator account, click `Parameter Dictionary` at the top, then click the `System Feature Configuration` page in the dropdown menu. On the page, check `Voice Cloning` and click Save Configuration. You will then see a `Voice Cloning` button in the top menu.

Log in to the control console using the super-administrator account, click [Voice Cloning] at the top, then [Voice Resources].

Click the Add button. In [Platform Name], select "Volcano Bidirectional Streaming TTS".

In [Voice Resource ID], enter your Volcano Engine voice resource ID (S_xxxxx), and press Enter after entering it.

In [Owner Account], select the system account you want to assign to. You can assign it to yourself. Then click Save.

## Stage 3: Cloning

If, after logging in, you click [Voice Cloning] -> [Voice Cloning] at the top and it shows [Your account has no voice resources; please contact an administrator to assign voice resources], that means you have not yet assigned a voice resource ID to this account in Stage 2. Go back to Stage 2 and assign voice resources to the corresponding account.

If, after logging in, you click [Voice Cloning] -> [Voice Cloning] at the top and can see the corresponding voice list, please continue.

In the list you will see the corresponding voices. Select one of the voice resources and click the [Upload Audio] button. After uploading, you can preview the sound or trim a clip. After confirming, click the [Upload Audio] button.
![Upload Audio](images/image-clone-integration-02.png)

After uploading the audio, in the list the corresponding voice will change to "Pending Cloning" status. Click the [Clone Now] button. It takes about 1 to 2 seconds to return a result.

If cloning fails, hover over the "Error Info" icon to see the reason for failure.

If cloning succeeds, in the list the corresponding voice will change to "Training Successful" status. At this point, you can click the Edit button in the [Voice Name] column to modify the name of the voice resource for easier selection later.

## Stage 4: Usage

Click [Agent Management] at the top, select any agent, and click [Configure Role].

For Text-to-Speech (TTS), select "Volcano Bidirectional Streaming TTS". In the list, find the voice resource whose name contains "Cloned Voice" (as shown), select it, and click Save.
![Select voice](images/image-clone-integration-03.png)

Next, you can wake up LittleWise and have a conversation with it.
