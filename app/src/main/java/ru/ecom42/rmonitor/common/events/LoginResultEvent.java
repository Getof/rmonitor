package ru.ecom42.rmonitor.common.events;

import ru.ecom42.rmonitor.common.models.Techdir;

public class LoginResultEvent extends BaseResultEvent{
    public Techdir rider;
    public String riderJson;
    public String jwtToken;
    public LoginResultEvent(int response, String riderJson, String jwtToken) {
        super(response);
        this.riderJson = riderJson;
        this.rider = Techdir.fromJson(riderJson);
        this.jwtToken = jwtToken;
    }
    public LoginResultEvent(int response, String message) {
        super(response,message);
    }
}
