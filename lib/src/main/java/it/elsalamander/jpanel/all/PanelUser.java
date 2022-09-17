package it.elsalamander.jpanel.all;

/*********************************************************************
 * Istanza di un utente
 * 
 * 
 * @author: Elsalamander
 * @data: 15 set 2022
 * @version: v1.0.0
 * 
 ********************************************************************/
public class PanelUser  {
    public final String password;
    public final boolean canEditFiles;
    public final boolean canChangeGroups;
    public final boolean canSendCommands;

    public PanelUser(String password, boolean canEditFiles, boolean canChangeGroups, boolean canSendCommands) {
        this.password = password;
        this.canEditFiles = canEditFiles;
        this.canChangeGroups = canChangeGroups;
        this.canSendCommands = canSendCommands;
    }

}
