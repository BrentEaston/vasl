package VASL.build.module.fullrules.PhaseClasses;

import VASL.build.module.fullrules.Constantvalues;
import VASL.build.module.fullrules.DataClasses.DataC;
import VASL.build.module.fullrules.DataClasses.Scenario;
import VASL.build.module.fullrules.Game.ScenarioC;
import VASSAL.build.GameModule;
// concrete class implementing iPhaseMVC
public class CCPhaseMVC implements iPhaseMVC {
    private AutomatedPhaseActions AutoActions;
    private Constantvalues.Phase CurrentPhasevalue ;
    private int CurrentTurnvalue;
    private Constantvalues.WhoCanDo CurrentPlayerTurnvalue;
    private boolean IsFinishedvalue;
    private PhaseObserverInterface PhaseObserver;

    public CCPhaseMVC(int ScenID) {
        ScenarioC scen  = ScenarioC.getInstance();
        Scenario Scendet = scen.getScendet();
        CurrentPlayerTurnvalue = Scendet.getPTURN();
        CurrentTurnvalue = Scendet.getCURRENTTURN();
        IsFinishedvalue = false;
        CurrentPhasevalue = Constantvalues.Phase.CloseCombat;
        AutoActions =new AutomatedPhaseActions(CurrentPlayerTurnvalue, ScenID);
    }

    public boolean JoinPhase() {
        /*' Called by EnterIntonewPhase and PhaseChange, Gameform.buSavScen
        ' Open Existing Scenario menu item and Open Campaign routine
        ' does not run routine called the first time a phase is entered, that is done in EnternewPhase*/
        return true;
    }

    public void EnterIntoNewPhase() {
        // called by Scenario.Phasechange and Scenario Startup routines
        // when entering for the first time
        // If coming back from previous phase, Phasechange sends to Joinphase instead

        // this is where value of phase is changed regardless of direction
        CurrentPhasevalue = Constantvalues.Phase.CloseCombat;
        AutoActions.SaveScenario(false); // save existing scenario
        GameModule.getGameModule().getChatter().send("You are now in new Phase: Database save complete");
        GameModule.getGameModule().getChatter().send("Now in Close Combat Phase");
        AutoActions.ShowHiddenInCC();
        AutoActions.PutHiddenOnBoard();
        JoinPhase();
        // ReportEvent.LognewPhase()
    }

    public void LeaveCurrentPhase() {
        // called by Quit menu item and ExitfromPhase
        // at quit, leave current phase

        // Do routines that need to be each time you stop during a phase
        // could handle some parts of cleanup before Exitfromphase

        // Do saving and updating routines that are required everytime a phase is left
        //UpdateOBUnitDatabase() : UpdateOBSWDatabase() : UpdateOBGunDatabase()
        //UpdateMapTerrainDatabase: WriteActiveScenariotoDatabase()
    }

    public void ExitFromPhaseBack() {

    }
    public void ExitFromPhaseForward() {
        PINTIUpd();
        ClearIllumination();
        LeaveCurrentPhase();
        // ReportEvent.LogPhaseHistory()
    }

    private void PINTIUpd() {
        /*' called by Scenario.ExitfromPhase
        ' removes PIN/TI status and counters at end of CC
        ' this is an automatic routine that is unavailable to the user.*/
    }
    private void ClearIllumination() {

    }

    public void RegisterObserver(PhaseObserverInterface PhaseObserverC) {
        // the Observer is the view as a whole NOT the phase
        PhaseObserver = PhaseObserverC;
    }

    public void RemoveObserver() {
        // no implementation required so far
    }

    private void NotifyObservers() {
        // called by SetLOSFPdrmValues
        // calls UpdateView on all observers
        PhaseObserver.UpdateView();
    }

    public Constantvalues.Phase getCurrentPhase () {return CurrentPhasevalue;}
    public Constantvalues.WhoCanDo getCurrentPlayerTurn() {return CurrentPlayerTurnvalue;}
    public int getCurrentTurn() {return CurrentTurnvalue;}
    public boolean IsFinished() {return IsFinishedvalue;}
}