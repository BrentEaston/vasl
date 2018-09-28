package VASL.build.module.fullrules.ObjectClasses;

import VASL.LOS.Map.Hex;
import VASL.LOS.Map.Location;
import VASL.build.module.fullrules.Constantvalues;
import VASL.build.module.fullrules.DataClasses.OrderofBattle;
import VASL.build.module.fullrules.DataClasses.OrderofBattleSW;
import VASL.build.module.fullrules.DataClasses.SupportWeapon;
import VASL.build.module.fullrules.Game.ScenarioC;
import VASL.build.module.fullrules.UtilityClasses.CommonFunctionsC;
import VASL.build.module.fullrules.UtilityClasses.ManageUpdateSWCommand;
import VASL.build.module.fullrules.UtilityClasses.ManageUpdateUnitCommand;
import VASSAL.build.GameModule;
import VASSAL.command.Command;

public class BaseSuppWeapc {
    private int myScenario;
    private Hex myHex;
    private Location myhexlocation;
    private Constantvalues.AltPos myhexPosition;
    private double myLevelinHex;
    private boolean myCX;
    private int myTurnArrives;
    private Constantvalues.Nationality myNationality;
    private int myCon_ID;
    private int myUnit_ID;
    private Constantvalues.Typetype myTypeType_ID;
    private int myHexEntSideCrossed;
    private int mySolID;
    private String myUnitName;
    private int myLOBLink;
    private Constantvalues.MovementStatus myMovementStatus;
    private Constantvalues.FortitudeStatus myFortitudeStatus;
    private Constantvalues.SWStatus mySWStatus;
    private Constantvalues.VisibilityStatus myVisibilityStatus;
    private Constantvalues.CombatStatus myCombatStatus;
    private Constantvalues.CharacterStatus myCharacterStatus;
    private boolean myPinned;
    private int myPP;
    private int myRepair;
    private int myDisPP;
    private boolean myCaptured;
    private int myOwner;
    private boolean myIsMG;
    private boolean myIsFT;
    private boolean myIsDC;

    public BaseSuppWeapc(int PassScenario, Hex PassHex, Location Passhexlocation, Constantvalues.AltPos PasshexPosition, double PassLevelinHex, boolean PassCX,
                         int PassTurnArrives, Constantvalues.Nationality PassNationality, int PassCon_ID, int PassUnit_ID, Constantvalues.Typetype PassTypeType_ID, int PassPP, int PassRepair, int PassDisPP,
                         int PassHexEntSideCrossed, int PassSolID, String PassUnitName, int PassLOBLink, Constantvalues.CombatStatus PassCombatStatus, Constantvalues.VisibilityStatus PassVisibilityStatus, Constantvalues.FortitudeStatus PassFortitudeStatus,
                         Constantvalues.SWStatus PassSWStatus, Constantvalues.MovementStatus PassMovementStatus, boolean PassPinned, int PassSW, Constantvalues.CharacterStatus PassCharacterStatus, boolean PassCaptured, int PassOwner) {


        myScenario = PassScenario;
        myHex = PassHex;
        myhexlocation = Passhexlocation;
        myhexPosition = PasshexPosition;
        myLevelinHex = PassLevelinHex;
        myCX = PassCX;
        myTurnArrives = PassTurnArrives;
        myNationality = PassNationality;
        myCon_ID = PassCon_ID;
        myUnit_ID = PassUnit_ID;
        myTypeType_ID = PassTypeType_ID;
        myHexEntSideCrossed = PassHexEntSideCrossed;
        mySolID = PassSolID;
        myUnitName = PassUnitName;
        myLOBLink = PassLOBLink;
        myMovementStatus = PassMovementStatus;
        myFortitudeStatus = PassFortitudeStatus;
        mySWStatus = PassSWStatus;
        myVisibilityStatus = PassVisibilityStatus;
        myCombatStatus = PassCombatStatus;
        myPinned = PassPinned;
        myCharacterStatus = PassCharacterStatus;
        myPP = PassPP;
        myDisPP = PassDisPP;
        myRepair = PassRepair;
        myCaptured = PassCaptured;
        myOwner = PassOwner;
        settypeflags();
    }

    public String getUnitName() {return myUnitName;}
    public int getScenario() {return myScenario;}
    public Hex getHex() {return myHex;}
    public void setHex(Hex value) {myHex = value;}
    public Location gethexlocation() {return myhexlocation;}
    public void sethexlocation(Location value) {myhexlocation = value;}
    public Constantvalues.AltPos gethexPosition() {return myhexPosition;}
    public void sethexposition(Constantvalues.AltPos value) {myhexPosition = value;}
    public double getLevelinHex() {return myLevelinHex;}
    public void setLevelinHex(double value) {myLevelinHex = value;}
    public boolean getCX() {return myCX;}
    public void setCX(boolean value) {myCX = value;}
    public int getTurnArrives() {return myTurnArrives;}
    public Constantvalues.Nationality getNationality() {return myNationality;}
    public int getCon_ID() {return myCon_ID;}
    public void setCon_ID(int value) {myCon_ID = value;}
    public int getSW_ID() {return myUnit_ID;}
    public Constantvalues.Typetype getType_ID() {return myTypeType_ID;}
    public void setType_ID(Constantvalues.Typetype value) {myTypeType_ID = value;}
    public int getHexEntSideCrossed() {return myHexEntSideCrossed;}
    public void setHexEntSideCrossed(int value) {myHexEntSideCrossed = value;}
    public int getSolID() {return mySolID;}
    public void setSolID(int value) {mySolID = value;}
    public int getLOBLink() {return myLOBLink;}
    public Constantvalues.FortitudeStatus getFortitudeStatus() {return myFortitudeStatus;}
    public void setFortitudeStatus(Constantvalues.FortitudeStatus value) {myFortitudeStatus = value;}
    public Constantvalues.MovementStatus getMovementStatus() {return myMovementStatus;}
    public void setMovementStatus(Constantvalues.MovementStatus value) {myMovementStatus = value;}
    public Constantvalues.SWStatus getSWStatus() {return mySWStatus;}
    public void setSWStatus(Constantvalues.SWStatus value) {mySWStatus = value;}
    public Constantvalues.CombatStatus getCombatStatus() {return myCombatStatus;}
    public void setCombatStatus(Constantvalues.CombatStatus value) {myCombatStatus = value;}
    public Constantvalues.VisibilityStatus getVisibilityStatus() {return myVisibilityStatus;}
    public void setVisibilityStatus(Constantvalues.VisibilityStatus value) {myVisibilityStatus = value;}
    public boolean getPinned() {return myPinned;}
    public void setPinned(boolean value) {myPinned = value;}
    public Constantvalues.CharacterStatus getCharacterStatus() {return myCharacterStatus;}
    public void setCharacterStatus(Constantvalues.CharacterStatus value ) {myCharacterStatus = value;}
    public int getDisPP() {return myDisPP;}
    public void setDisPP(int value) {myDisPP = value;}
    public int getPP() {return myPP;}
    public void setPP(int value) {myPP = value;}
    public int getRepair() {return myRepair;}
    public boolean isCaptured() {return myCaptured;}
    public void setCaptured (boolean captured){myCaptured = captured;}
    public int getOwner() {return myOwner;}
    public void setOwner(int value) {myOwner = value;}
    public boolean IsMG() {return myIsMG;}
    public boolean IsFT() {return myIsFT;}
    public boolean IsDC() {return myIsDC;}

    // public MovingSuppWeapi[] getMovingSW(){return null;}
    public FiringSuppWeapi[] getFiringSW(){return null;}

    /*public boolean IsUnitALeader() {
        return if(this.getType_ID() >= ConstantClassLibrary.ASLXNA.Utype.LdrHero And Me.LOBLink <= ConstantClassLibrary.ASLXNA.Utype.SMC, True, False)
    }
    public boolean IsUnitASMC() {
        return If(Me.Type_ID >= ConstantClassLibrary.ASLXNA.Utype.LdrHero And Me.LOBLink <= ConstantClassLibrary.ASLXNA.Utype.Commissar, True, False)
    }*/

    public boolean IsInCrestStatus() {
        return false;
    }
        /*'public Function IsUnitInexperienced() As Boolean
                '    If (myUnitClass = ConstantClassLibrary.ASLXNA.UClass.green Or myUnitClass = ConstantClassLibrary.ASLXNA.UClass.Agreen Or myUnitClass = ConstantClassLibrary.ASLXNA.UClass.Sgreen Or myUnitClass = ConstantClassLibrary.ASLXNA.UClass.ASgreen Or
                '        myUnitClass = ConstantClassLibrary.ASLXNA.UClass.Conscript Or myUnitClass = ConstantClassLibrary.ASLXNA.UClass.AConscript Or myUnitClass = ConstantClassLibrary.ASLXNA.UClass.ASConscript Or myUnitClass = ConstantClassLibrary.ASLXNA.UClass.SConscript) Then return True Else return False
                'End Function
                'public Function IsUnitGreen() As Boolean
                '    If (myUnitClass = ConstantClassLibrary.ASLXNA.UClass.green Or myUnitClass = ConstantClassLibrary.ASLXNA.UClass.Agreen Or myUnitClass = ConstantClassLibrary.ASLXNA.UClass.Sgreen Or myUnitClass = ConstantClassLibrary.ASLXNA.UClass.ASgreen) Then return True Else return False
                'End Function*/
    public boolean IsUnitEncircled() {
        return (getFortitudeStatus() == Constantvalues.FortitudeStatus.Enc_Wnd ||
                getFortitudeStatus() == Constantvalues.FortitudeStatus.Encircled ||
                getFortitudeStatus() == Constantvalues.FortitudeStatus.Fan_Wnd_Enc ||
                getFortitudeStatus() == Constantvalues.FortitudeStatus.Fan_Enc ? true: false);
    }

    /*public Function HasWallAdvantage() As Boolean
    return If(myhexPosition = ConstantClassLibrary.ASLXNA.AltPos.WallAdv Or (myhexPosition >= ConstantClassLibrary.ASLXNA.AltPos.WACrestStatus1 And myhexPosition <= ConstantClassLibrary.ASLXNA.AltPos.WACrestStatus6), True, False)
    End Function*/
    public boolean WeaponIsLost() {
        // called by DroppingSW.DropIt
        // if sw dropped in certain location it is lost; handles location check and processes loss
//        Dim Maptables As MapDataClassLibrary.ASLXNA.MapDataC = MapDataClassLibrary.ASLXNA.MapDataC.GetInstance("", 0); // use null values for parameters when sure instance exists
//        Dim LocationCol As IQueryable (Of MapDataClassLibrary.GameLocation) =Maptables.LocationCol;

        // THIS IS NOT A GOOD METHOD; ONCE location SETTLES DOWN CONVERT THIS TO db QUERIES

        boolean IsLost= false;
        /*Dim IsTerrChk = New TerrainClassLibrary.ASLXNA.IsTerrain(LocationCol)
        Dim Usinghex = New TerrainClassLibrary.ASLXNA.LevelChecks(LocationCol)
        Dim Baseloc As MapDataClassLibrary.GameLocation = Usinghex.GetLocationatLevelInHex(CInt(Me.Hexnum), 0)
        Dim Basehexloc As Integer = Baseloc.Location
        If IsTerrChk.
        IsLocationTerrainA(CInt(Me.Hexnum), CInt(Me.hexlocation), ConstantClassLibrary.ASLXNA.Location.Marshtype) Then
                IsLost = True
        ElseIf IsTerrChk.
        IsLocationTerrainA(CInt(Me.Hexnum), CInt(Me.hexlocation), ConstantClassLibrary.ASLXNA.Location.ShallowStreamtype)
        Then
                IsLost = True
        ElseIf IsTerrChk.
        IsLocationTerrainA(CInt(Me.Hexnum), CInt(Me.hexlocation), ConstantClassLibrary.ASLXNA.Location.DeepStreamtype)
        Then
                IsLost = True
        ElseIf IsTerrChk.
        IsLocationTerrainA(CInt(Me.Hexnum), CInt(Me.hexlocation), ConstantClassLibrary.ASLXNA.Location.WaterObstacletype)
        Then
                IsLost = True
        ElseIf IsTerrChk.
        IsLocationTerrainA(CInt(Me.Hexnum), CInt(Me.hexlocation), ConstantClassLibrary.ASLXNA.Location.Blazetype) Then
                IsLost = True
        ElseIf IsTerrChk.
        IsLocationTerrainA(CInt(Me.Hexnum), CInt(Me.hexlocation), ConstantClassLibrary.ASLXNA.Location.bridgetype) And
                (CInt(Me.hexlocation) = ConstantClassLibrary.ASLXNA.Location.BeneathBridge Or(CInt(Me.hexlocation) < ConstantClassLibrary.ASLXNA.Location.StBr14 And CInt(Me.hexlocation) > ConstantClassLibrary.ASLXNA.Location.FoBr))
        Then
        ' if not on bridge then check it is a water hex
        If Basehexloc >=ConstantClassLibrary.ASLXNA.Location.StBr14 And
        Basehexloc <= ConstantClassLibrary.ASLXNA.Location.FoBr Then 'or

        IsLost = True
        End If
        ElseIf CInt (Me.hexlocation) = ConstantClassLibrary.ASLXNA.Location.BeneathPier
        And Basehexloc = ConstantClassLibrary.ASLXNA.Location.Pier Then
                IsLost = True
        End If*/
        if (IsLost) {
            this.setOwner(0);
            this.setSWStatus(Constantvalues.SWStatus.Malfunctioned);
            GameModule.getGameModule().getChatter().send(this.getUnitName() + " is lost and removed due to location");
            return true;
        } else {
            return false;
        }
    }
    public void settypeflags() {
        CommonFunctionsC comfun = new CommonFunctionsC(myScenario);
        SupportWeapon weapontocheck = comfun.GetSupportWeapon(Integer.toString(myLOBLink));
        switch (weapontocheck.getWeaponType()) {
            case LMGun: case MMGun: case HMGun: case H50cal:
                myIsMG=true;
                myIsFT=false;
                myIsDC=false;
                break;
            case FThr:
                myIsMG=false;
                myIsFT=true;
                myIsDC=false;
                break;
            case DemoC:
                myIsMG=false;
                myIsFT=false;
                myIsDC=true;
                break;
            default:
                myIsMG=false;
                myIsFT=false;
                myIsDC=false;
                break;
        }
    }
    public boolean UpdateSWStatus(SuppWeapi PassSW) {
        // as part of BaseSuppWeapc this will be common across all SW
        // get SupportWeapon that matches the SuppWeapi
        ManageUpdateSWCommand manageupdateswcommand = new ManageUpdateSWCommand();
        Command newcommand = manageupdateswcommand.CreateCommand(PassSW, Constantvalues.UnitCommandtype.basesw);
        manageupdateswcommand.ProcessCommand(newcommand);

        // this may no longer be needed as above may handle for both local and remote
        CommonFunctionsC comfun = new CommonFunctionsC(PassSW.getbaseSW().getScenario());
        OrderofBattleSW UpdateUnit = comfun.getUnderlyingOBSWunitforSuppWeapi(PassSW.getbaseSW().getSW_ID(),  PassSW.getbaseSW().getUnitName());

        if (UpdateUnit != null) {
            UpdateUnit.setOwner(PassSW.getbaseSW().getOwner());
            UpdateUnit.setCaptured(PassSW.getbaseSW().myCaptured);
            UpdateUnit.setCombatStatus(PassSW.getbaseSW().getCombatStatus());
            UpdateUnit.setStatus(PassSW.getbaseSW().getSWStatus());
            UpdateUnit.setVisibilityStatus(PassSW.getbaseSW().getVisibilityStatus());
            return true;
        }
        return false;
    }
}
