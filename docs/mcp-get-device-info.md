# How to Obtain Device Information via MCP Methods

This tutorial will guide you through using MCP methods to obtain device information.

Step 1: Customize your `agent-base-prompt.txt` file.

Copy the contents of the `agent-base-prompt.txt` file in the `xiaozhi-server` directory into your `data` directory, and rename it to `.agent-base-prompt.txt`.

Step 2: Edit the `data/.agent-base-prompt.txt` file. Find the `<context>` tag and add the following line inside the tag's content:
```
- **Device ID:** {{device_id}}
```

After the addition, the contents of the `<context>` tag in your `data/.agent-base-prompt.txt` file should look roughly like:
```
<context>
[Important! The following information is provided in real time. No tool call is needed; please use it directly:]
- **Device ID:** {{device_id}}
- **Current time:** {{current_time}}
- **Today's date:** {{today_date}} ({{today_weekday}})
- **Today's lunar date:** {{lunar_date}}
- **User's city:** {{local_address}}
- **Weather for the next 7 days in the area:** {{weather_info}}
</context>
```

Step 3: Modify the `data/.config.yaml` file. Find the `agent-base-prompt` configuration. The content before modification is:
```
prompt_template: agent-base-prompt.txt
```
Change it to:
```
prompt_template: data/.agent-base-prompt.txt
```

Step 4: Restart your xiaozhi-server service.

Step 5: Add a parameter named `device_id` of type `string` with description `Device ID` to your MCP method.

Step 6: Wake up LittleWise again and have it call the MCP method. Check whether your MCP method can obtain the `Device ID`.
