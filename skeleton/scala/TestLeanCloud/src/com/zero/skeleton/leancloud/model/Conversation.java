package com.zero.skeleton.leancloud.model;

import java.util.ArrayList;

/**
 * Created by zero on 4/27/16.
 */
public class Conversation {
    /**
     * optional
     * ext for dev
     */
    public Object attr;

    /**
     * members: client id list of chat
     */
    public ArrayList<String> m;

    /**
     * optional
     * name of this chat
     */
    public String name;

    /**
     * optional
     * transient: is this a temporary chat or not
     */
    public Boolean tr;

    /**
     * optional
     * system: is this a system chat or not
     */
    public Boolean sys;

}
