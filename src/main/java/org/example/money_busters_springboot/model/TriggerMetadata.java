package org.example.money_busters_springboot.model;


public class TriggerMetadata {

    private String triggerName;
    private String tableName;
    private String triggerType;
    private String triggeringEvent;
    private String status;
    private String triggerBody;

    public TriggerMetadata() {
    }

    public TriggerMetadata(String triggerName, String tableName, String triggerType,
                           String triggeringEvent, String status, String triggerBody) {
        this.triggerName = triggerName;
        this.tableName = tableName;
        this.triggerType = triggerType;
        this.triggeringEvent = triggeringEvent;
        this.status = status;
        this.triggerBody = triggerBody;
    }

    public String getTriggerName() {
        return triggerName;
    }

    public void setTriggerName(String triggerName) {
        this.triggerName = triggerName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public String getTriggeringEvent() {
        return triggeringEvent;
    }

    public void setTriggeringEvent(String triggeringEvent) {
        this.triggeringEvent = triggeringEvent;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTriggerBody() {
        return triggerBody;
    }

    public void setTriggerBody(String triggerBody) {
        this.triggerBody = triggerBody;
    }

    @Override
    public String toString() {
        return "TriggerMetadata{" +
                "triggerName='" + triggerName + '\'' +
                ", tableName='" + tableName + '\'' +
                ", triggerType='" + triggerType + '\'' +
                ", triggeringEvent='" + triggeringEvent + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
