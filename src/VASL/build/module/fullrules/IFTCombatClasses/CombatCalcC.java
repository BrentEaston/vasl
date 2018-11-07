package VASL.build.module.fullrules.IFTCombatClasses;

import VASL.LOS.Map.Hex;
import VASL.build.module.fullrules.DataClasses.IFTMods;
import VASL.build.module.fullrules.DataClasses.Scenario;
import VASL.build.module.fullrules.Game.ScenarioC;
import VASL.build.module.fullrules.LOSClasses.LOSSolution;
import VASL.build.module.fullrules.LOSClasses.LOSThreadManagerC;
import VASL.build.module.fullrules.LOSClasses.ThreadedLOSCheckCommonc;
import VASL.build.module.fullrules.ObjectClasses.CombatTerrain;
import VASL.build.module.fullrules.ObjectClasses.PersUniti;
import VASL.build.module.fullrules.ObjectClasses.ScenarioCollectionsc;
import VASL.build.module.fullrules.Constantvalues;
import VASL.build.module.fullrules.ObjectClasses.SuppWeapi;
import VASL.build.module.fullrules.TerrainClasses.LevelChecks;
import VASL.build.module.fullrules.TerrainClasses.TerrainChecks;
import VASL.build.module.fullrules.UtilityClasses.CombatUtil;
import VASSAL.build.GameModule;

import java.util.LinkedList;

public class CombatCalcC implements CombatCalci {

    private LinkedList<IFTMods> pFinalDRMList = new LinkedList<IFTMods>();
    private LinkedList<IFTMods> HoldTerrainDRMList = new LinkedList<IFTMods>();

    //private GetALocationFromMap Getlocs;
    private ScenarioCollectionsc Scencolls = ScenarioCollectionsc.getInstance();
    private LinkedList<LOSSolution> ValidSolutions = new LinkedList<LOSSolution>();
    private Scenario Scendet;
    private LOSThreadManagerC ThreadManager = new LOSThreadManagerC();
    // public ObjLink As ObjectFactoryvalues.MenuObjectCreation = new ObjectFactoryvalues.MenuObjectCreation
    private LinkedList<PersUniti> FireGroupToUse = new LinkedList<PersUniti>();
    private LinkedList<PersUniti> TargetGroupToUse = new LinkedList<PersUniti>();
    private ScenarioC scen = ScenarioC.getInstance();

    public CombatCalcC(LinkedList<LOSSolution> PassValidSolutions) {
    
        ValidSolutions = PassValidSolutions;

    }
    public LinkedList<IFTMods> getFinalDRMList() {return pFinalDRMList;}
    
    public int CalcCombatFPandDRM(LinkedList<PersUniti> PassFireGroupToUse, LinkedList<PersUniti> PassTargetGroupToUse, Scenario PassScendet, int Usingsol) {
        // Called by IFT.ManageCombatSolutionDetermination
        // determines the FP and drm applicable to each target in a ValidSolution
        // all other methods are called by this one

        Scendet = PassScendet;
        FireGroupToUse = PassFireGroupToUse;
        TargetGroupToUse = PassTargetGroupToUse;

        // set up required variables
        int range = 0;
        double TotalFP=0;
        boolean UsingSprayFire = false; // holds value of variable used to track multihex targets
        int FDRM  = 0; // holds value of all firer-based drm
        boolean SmokePresent = false; //int HeroDRM= 0; String HeroName = "";
        Constantvalues.Utype UnitSize = Constantvalues.Utype.None;
        //int LdrMod = 0; int TestLdrMod= 0; String LdrName = "";
        //boolean CXTest = false;
        //boolean EncircTest = false; // flag for Encirc DRM
        //int response;
        boolean alreadymoved = false;
        double Levelcheck  = 0; double TotalTargetLevel = 0;
        boolean SprayFireRequired = false; // hold values of variable used to track multihex targets
        //LevelChecks LevelChk = new LevelChecks(Mapcol);
        LinkedList<IFTMods> UsingTerrainDRM = new LinkedList<IFTMods>();
        pFinalDRMList.clear();
        // check various combat parameters are correctly set
        for (PersUniti TargetUnit: TargetGroupToUse) {
            // check for already moved - used if Def fire- still needs to be ADDED
            if (TargetUnit.getbaseunit().getMovementStatus() == Constantvalues.MovementStatus.Moved) {alreadymoved = true;}
        }
        UsingSprayFire = SprayFireTest(TargetGroupToUse, SprayFireRequired);
        if (!UsingSprayFire && SprayFireRequired) {return -99;} // fire solution no longer valid

        // Now doing Firer-based IFT DRMs which apply to all Firers and
        // Targets so only calculate once - using input from FiringUnit loop above
        // Unit based DRM - CX and Encric, leaders and heroes
        FiringDRMs firingdrms = CalcFiringDRMs(UsingSprayFire, Usingsol);
        // SMC if present
        if (firingdrms.getLeaderdrm() != 0) {FDRM = FDRM + firingdrms.getLeaderdrm();}
        // Hero if present
        if (firingdrms.getHeroicdrm() != 0) {FDRM = FDRM + firingdrms.getHeroicdrm();}
        // Adding CX +1
        if (firingdrms.getCXapplies()) {FDRM += 1;}
        // Adding Encirc +1
        if (firingdrms.getEncircapplies()) {FDRM += 1;}

        // Iterate through TargetGroup and Firegroup adding Firepower
        // and determining FP adjustments - calc FP and drm for all TargetUnit


        boolean AddDRM = true; IFTMods NewDRM;
        for (PersUniti TargetUnit: TargetGroupToUse) {
            // create list of all drms that apply
            // add drm that apply to all Targets - need to add to DRMList to support info display on Display form
            if (firingdrms.getLeaderdrm() != 0) {
                for (IFTMods DRMTest : pFinalDRMList) {
                    if (DRMTest.getDRMType() == Constantvalues.IFTdrm.Leader) {
                        if (firingdrms.getLeaderdrm() < DRMTest.getDRM()) {
                            DRMTest.setDRM(firingdrms.getLeaderdrm());
                            AddDRM = false;
                            break;
                        }
                    }
                }
                if (AddDRM) {
                    NewDRM = new IFTMods(firingdrms.getLeaderdrm(), Constantvalues.IFTdrm.Leader, 0, Constantvalues.Typetype.Personnel, null, null, "Leadership"); // the nulls help confirm that this is a firer-based drm that applies to all targets - used in display form
                    pFinalDRMList.add(NewDRM);
                }
            }
            // Hero if present
            AddDRM = true;
            if (firingdrms.getHeroicdrm() != 0) {
                for (IFTMods DRMTest : pFinalDRMList) {
                    if (DRMTest.getDRMType() == Constantvalues.IFTdrm.Hero) {
                        AddDRM = false;
                        break;
                    }
                }
                if (AddDRM) {
                    NewDRM = new IFTMods(firingdrms.getHeroicdrm(), Constantvalues.IFTdrm.Hero, 0, Constantvalues.Typetype.Personnel, null, null, "Hero");  // the null help confirm that this is a firer-based drm that applies to all targets - used in display form
                    pFinalDRMList.add(NewDRM);
                }
            }
            // Adding CX +1
            AddDRM = true;
            if (firingdrms.getCXapplies()) {
                for (IFTMods DRMTest : pFinalDRMList) {
                    if (DRMTest.getDRMType() == Constantvalues.IFTdrm.FirerCX) {
                        AddDRM = false;
                        break;
                    }
                }
                if (AddDRM) {
                    NewDRM = new IFTMods(1, Constantvalues.IFTdrm.FirerCX, 0, Constantvalues.Typetype.Personnel, null, null, "CX");  // the nulls help confirm that this is a firer-based drm that applies to all targets - used in display form
                    pFinalDRMList.add(NewDRM);
                }
            }
            // Adding Encirc +1
            AddDRM = true;
            if (firingdrms.getEncircapplies()) {
                for (IFTMods DRMTest : pFinalDRMList) {
                    if (DRMTest.getDRMType() == Constantvalues.IFTdrm.FirerEnc) {
                        AddDRM = false;
                        break;
                    }
                }
                if (AddDRM) {
                    NewDRM = new IFTMods(1, Constantvalues.IFTdrm.FirerEnc, 0, Constantvalues.Typetype.Personnel, null, null, "Encirc");  // the nulls help confirm that this is a firer-based drm that applies to all targets - used in display form
                    pFinalDRMList.add(NewDRM);
                }
            }
            // end of Firer-based mods

            // Now do FP
            TotalFP=0; // reset TotalFP for each target
            if (Usingsol == -1) {  // using all validsols
                for (LOSSolution Validsol : ValidSolutions) {
                    TotalFP += DoFPCalc(TargetUnit, Validsol.getID(), FireGroupToUse, UsingSprayFire, firingdrms.getFGSize());
                }
            } else {
                TotalFP = DoFPCalc(TargetUnit, Usingsol, FireGroupToUse, UsingSprayFire, firingdrms.getFGSize());
            }
            CombatUtil CombatMeth = new CombatUtil();
            TotalFP = CombatMeth.FPRoundingDown(TotalFP);
            TargetUnit.getTargetunit().setAttackedbyFP((int) (TotalFP));

            // Combatdrm does not need to be called if Target in same location - ADD IN THIS TEST
            // now add Target-based DRM
            int Soldrm = 0;
            int totaldrm = 0;
            if (Usingsol == -1) {
                boolean FirstLoop = true;
                for (LOSSolution Validsol : ValidSolutions) {
                    Soldrm = CombatDRM(alreadymoved, range, TargetUnit, Validsol.getID());
                    if (FirstLoop) {
                        totaldrm = Soldrm;
                        for (IFTMods AddTheDRM : HoldTerrainDRMList) {
                            UsingTerrainDRM.add(AddTheDRM);
                        }
                        HoldTerrainDRMList.clear();
                        FirstLoop = false;
                    } else {
                        if (Soldrm > totaldrm) {
                            for (IFTMods AddTheDRM : HoldTerrainDRMList) {
                                UsingTerrainDRM.add(AddTheDRM);
                            }
                            HoldTerrainDRMList.clear();
                            totaldrm = Soldrm;
                        }
                    }
                }
            } else {
                totaldrm = CombatDRM(alreadymoved, range, TargetUnit, Usingsol);
                for (IFTMods AddTheDRM : HoldTerrainDRMList) {
                    UsingTerrainDRM.add(AddTheDRM);
                }
                HoldTerrainDRMList.clear();
            }
            for (IFTMods AddNewDRM : UsingTerrainDRM) {
                pFinalDRMList.add(AddNewDRM);
            }
            TargetUnit.getTargetunit().setAttackedbydrm(totaldrm + FDRM);
            if (TargetUnit.getTargetunit().getAttackedbydrm() == 99) { // LOS blocked by Hindrance=>6
                return -99;
            } else {
                //CombatReport.AddTarget(TargetUnit)
            }
        }
        return (int)(TotalFP);
    }

    private int DetermineMaxCombatRange(PersUniti FiringUnit, LinkedList<PersUniti> TargetGroupToUse, boolean UsingSprayFire) {
    //Dim MapGeo as mapgeovalues.mapgeoc = MapGeovalues.MapGeoC.GetInstance(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0) // can use null values if sure instance already exists
        int TestRange = 0; int range = 0;
        Hex FiringHex; Hex TargetHex;
        if (UsingSprayFire) {
            for (PersUniti TargetUnit : TargetGroupToUse) {
                FiringHex = scen.getGameMap().getHex(FiringUnit.getbaseunit().getHexName());
                TargetHex = scen.getGameMap().getHex(TargetUnit.getbaseunit().getHexName());
                TestRange = scen.getGameMap().range(FiringHex, TargetHex, scen.getGameMap().getMapConfiguration());
                if (TestRange > range) {
                    range = TestRange;
                } // determine max range
            }
            return range;
        } else {
            FiringHex = scen.getGameMap().getHex(FiringUnit.getbaseunit().getHexName());
            TargetHex = scen.getGameMap().getHex(TargetGroupToUse.getFirst().getbaseunit().getHexName());
            return scen.getGameMap().range(FiringHex, TargetHex, scen.getGameMap().getMapConfiguration());
        }
    }
    private int CombatLdrDRM(LinkedList<PersUniti> FireGroup, boolean Ldrpresent, int UsingSol, FiringDRMs firingdrms) {
        // called by IFT.CalcCombatFPandDRM
        // calculates overall SMC drm and if leader present to avoid cowering
        // in multi-location FG leader must be present in every hex to direct
        int Testldrmod = 0;
        int BestInHex = 5;  // 5 is random value to allow for + leaders
        int BestinEach = -5;  // -5 is random value to allow for ldr common to each hex
        int HexesInFG = 0;
        for (LOSSolution ValidSol: ValidSolutions) {
            if (UsingSol == -1 || UsingSol == ValidSol.getID()) {  // AndAlso TargetUnit.LocIndex = ValidSol.SeenLOSIndex Then '-1 forces use of all valid solutions - needed in actual fire resolution for multi-hex fire group; if not -1 then checking hex by hex as in DFFMVCPattern
                //if UsingSol = ValidSol.ID Then
                // NEED TO RESOLVE if THESE CHECKS NEEDED - AUG 14
                //if ValidSol.HexesInLOS.Count = 0 Then
                //    for ( ComTer As Objectvalues.CombatTerrain In LOSTest.TempCombatTerrCol  ' Game.Scenario.IFT.CombatTerrCol
                //        if ComTer.SolID = ValidSol.ID Then ValidSol.AddtoLOSList(ComTer)
                //    Next
                //Else
                //    'MessageBox.Show("No Need to add Hexes to HexesInLOS; already there", "IFTC.CombatDRM")
                //End if
                //if ValidSol.AltHexesInLOS.Count = 0 Then
                //    for ( Althex As CombatTerrainvalues.AltHexGTerrain In ThreadManager.AltHexLOSGroup
                //        if Althex.TempSolID = ValidSol.ID Then ValidSol.AddtoAltHexList(Althex)
                //    Next
                //End if
                // WHY LOOP THROUGH COMBAT HEXES?
                for (CombatTerrain CombatHex: ValidSol.getHexesInLOS()) {
                    if (CombatHex.getSolID() != ValidSol.getID()) {
                        continue;
                    }
                    Ldrpresent = false;
                    if (CombatHex.IsFirer()) {  //Hexrole = constantvalues.Hexrole.Firer Or Combathex.Hexrole = constantvalues.Hexrole.FirerInt Or Combathex.Hexrole = constantvalues.Hexrole.FirerTargetInt Or Combathex.Hexrole = constantvalues.Hexrole.FirerTarget Then
                        // check each hex in Combat terrain collection; if a firer
                        // hex, leader must be present for Fire Direction
                        for (PersUniti FiringUnit : FireGroup) {
                            if (FiringUnit.getbaseunit().getUnittype() == Constantvalues.Utype.LdrHero &&
                                    CombatHex.getHexName() == FiringUnit.getbaseunit().getHexName()) {
                                if (FiringUnit.getFiringunit().getUseHeroOrLeader() == Constantvalues.Utype.Leader) {
                                    Ldrpresent = true;
                                } else if (FiringUnit.getFiringunit().getUseHeroOrLeader() == Constantvalues.Utype.Hero) {
                                } else {
                                    // need user to choose which drm to use
                                    // choice could be different in different tactical situations
                                    // Game.contextshowing = true
                                    // implement in a new way
                                    int WhichtoUse = 0;
                                    /*int WhichtoUse = CInt(InputBox("Do you wish to use (1) SMC DRM or (2) Hero DRM?" &
                                    vbCrLf & vbCrLf & "Enter 1 for SMC or 2 for Hero:", "" &
                                    FiringUnit.BasePersUnit.UnitName & " is a LeaderHero"));*/
                                    if (WhichtoUse == 1) {
                                        FiringUnit.getFiringunit().setUseHeroOrLeader(Constantvalues.Utype.Leader);
                                        Ldrpresent = true;
                                    } else {
                                        FiringUnit.getFiringunit().setUseHeroOrLeader(Constantvalues.Utype.Hero);
                                    }
                                    //Game.contextshowing = false
                                }
                            } else if (FiringUnit.getbaseunit().IsUnitALeader() && CombatHex.getHexName() == FiringUnit.getbaseunit().getHexName()) {
                                // check if ldr present
                                Ldrpresent = true;
                            }
                        }
                        if (Ldrpresent == false) {
                            // no leader in hex; leader cannot direct FG
                            //ldrname = "";
                            //MsgBox("No leader present in every hex. No SMC Direction")
                            return 0;
                        }
                    }
                }
                // if code reaches here, there is a leader in every hex
                for (CombatTerrain Combathex: ValidSol.getHexesInLOS()){
                    if (Combathex.getHexrole() == Constantvalues.Hexrole.Firer ||
                            Combathex.getHexrole() == Constantvalues.Hexrole.FirerInt ||
                            Combathex.getHexrole() == Constantvalues.Hexrole.FirerTargetInt ||
                            Combathex.getHexrole() == Constantvalues.Hexrole.FirerTarget) {
                        // now determine best common ldr drm
                        BestInHex = 5; Testldrmod = 0; HexesInFG += 1;
                        for (PersUniti FiringUnit: FireGroup) {
                            if ((FiringUnit.getbaseunit().getUnittype() == Constantvalues.Utype.Leader &&
                                    Combathex.getHexName() == FiringUnit.getbaseunit().getHexName()) ||
                                    (FiringUnit.getbaseunit().getUnittype() == Constantvalues.Utype.LdrHero &&
                                    FiringUnit.getFiringunit().getUseHeroOrLeader() == Constantvalues.Utype.Leader &&
                                    Combathex.getHexName() == FiringUnit.getbaseunit().getHexName())) {
                                // find the best leader in this hex
                                Testldrmod = FiringUnit.getFiringunit().getLdrDRM();
                                if (Testldrmod < BestInHex) {
                                    BestInHex = Testldrmod;
                                    firingdrms.setLeaderName(firingdrms.getLeaderName() + FiringUnit.getbaseunit().getUnitName());
                                }
                            }
                        }
                        if (BestInHex > BestinEach) {
                            // stores value of best common ldr drm
                            BestinEach = BestInHex;
                        }
                    }
                }
            }
         }
         // sets value of function and parameters to be returned
        int LdrDRM= (BestinEach == -5 ? 0: BestinEach);
        if (HexesInFG > 1) {
            firingdrms.setLeaderName("Ldr in each FG loc");
            GameModule.getGameModule().getChatter().send("Leaders present in all  hexes. DRM is:" + (java.lang.Integer.toString((BestinEach))));
        }
        Ldrpresent = true;
        firingdrms.setLeaderdrm(LdrDRM);
        return LdrDRM;
    }

    private boolean SprayFireTest(LinkedList<PersUniti> TargetGroupToUse, boolean SprayFireRequired) {
        double LevelCheck; String targethex=""; // response As DialogResult
        SprayFireRequired = false;
        if (TargetGroupToUse.size() == 1) {return false;} // no spray fire possible
        PersUniti FirstTarget = TargetGroupToUse.getFirst();
        LevelCheck = FirstTarget.getbaseunit().getLevelinHex();
        targethex = FirstTarget.getbaseunit().getHexName();
        for (PersUniti TargetUnit: TargetGroupToUse) {
            if (!(targethex.equals(TargetUnit.getbaseunit().getHexName())) || (TargetUnit.getbaseunit().getLevelinHex() != LevelCheck)){
                // multilocation target group
                return true;
                //  Display the message box and save the response, Yes or No.
                // implement in  a different way
                /*response = MessageBox.Show("Do you wish to use Spraying Fire (Y/N)?", "Multi Hex Target Group Found", MessageBoxButtons.YesNo)
                if response = System.Windows.Forms.DialogResult.Yes Then return true*/
            }
        }
        // if here then no spray fire chosen
        return false;
    }

    private void GetCombatFP(PersUniti Firingunit, PersUniti TargetUnit, double TotalTargetlevel, int range, int FGSize, Constantvalues.Utype UnitSize, boolean UsingSprayFire){

        int UnitRange; double RangeFactor = 1;
        Constantvalues.CombatStatus FirerStatus; // holds status value of firing unit (inf or mg)
        int BaseFP;   // holds LOB FP value of firing unit (inf or mg)
        //LevelChecks LevelChk = new LevelChecks(Firingunit.getbaseunit().gethexlocation());
        double leveldifference = 0; double TotalFirerLevel = 0; int targethex = 0;
        double UseAsRange = 0; String msg = "";
        //GetALocationFromMap Getlocs = new GetALocationFromMap(Mapcol);
        //GameLocation targloc = Getlocs.RetrieveLocationfromHex(TargetUnit.getbaseunit().getLOCIndex());
        // determine unit level
        //double Baselevel = LevelChk.GetLevelofLocation(); // use location=hexnumber always tests base location in hex
        TotalFirerLevel = Firingunit.getbaseunit().getLevelinHex() + Firingunit.getbaseunit().getHex().getBaseHeight();
        leveldifference = TotalFirerLevel - TotalTargetlevel;
        // determine base FP and range of unit, and if Assault Fire is possible
        UnitRange = Firingunit.getFiringunit().getBaseRange();
        BaseFP = Firingunit.getFiringunit().getBaseFP();
        // assign unit status
        FirerStatus = Firingunit.getFiringunit().getCombatStatus();
        if (UnitRange > 0) {   // UnitSize < constantvalues.Utype.SMC or  Then  'Unitsize is 0 when Firer is mg
            // checks for broken units and MG - this should not be necessary as
            // AddFirerUnit routine should not accept broken units
            // NEED TO FIX above JULY 2010
            //'if FirerStatus < constantvalues.OrderStatus.Brokenunit Or
            //'    (FirerStatus >= constantvalues.CombatStatus.Firing And
            //'     FirerStatus <> constantvalues.SWStatus.Brokendown And FirerStatus <> constantvalues.SWStatus.Dis_Broken) Then
            if (FirerStatus == Constantvalues.CombatStatus.Firing || FirerStatus == Constantvalues.CombatStatus.None) {
                // Start FP calc with base firepower
                // 'TempFP = BaseFP
                // Determine combat range
                Hex FiringHex = scen.getGameMap().getHex(Firingunit.getbaseunit().getHexName());
                Hex TargetHex = scen.getGameMap().getHex(TargetUnit.getbaseunit().getHexName());
                range = scen.getGameMap().range(FiringHex, TargetHex, scen.getGameMap().getMapConfiguration());

                if (range > 0) {
                    UseAsRange = range;
                } else { // same hex combat
                    UseAsRange = Math.abs(TargetUnit.getbaseunit().getLevelinHex() - Firingunit.getbaseunit().getLevelinHex());
                }
                // Set FiringUnit FP based on range impacts
                Firingunit.getFiringunit().RangeModification(UseAsRange, leveldifference, TargetUnit);
                // at this stage, after each sub TempFP=FiringUnit.CombatFP
                // Area Fire
                Firingunit.getFiringunit().AreaFireModification(FGSize, TargetUnit.getbaseunit().gethexlocation());
                // Pinned
                Firingunit.getFiringunit().PinnedModification();
                // Adv Fire
                Firingunit.getFiringunit().AdvancingFireModification(Scendet.getPhase());
                // Target Concealed
                Firingunit.getFiringunit().FireVsConcealedModification(TargetUnit);
                // First firer
                Firingunit.getFiringunit().FirstFireModification();
                // Spray Fire
                Firingunit.getFiringunit().SprayFireModification(UsingSprayFire);
                // Assault Fire check
                Firingunit.getFiringunit().AssaultFireModification(Scendet.getPhase());
                if (Firingunit.getFiringunit().getUseHeroOrLeader() == Constantvalues.Utype.Leader) {
                    Firingunit.getFiringunit().ResetCombatFP();
                    double Forcerecalc = Firingunit.getFiringunit().getCombatFP();
                } else {
                    //GameForm.UpdateListBoxItem(GameForm.lbFP, i, Str(FiringUnit.CombatFP))
                }
                if ((UnitSize == Constantvalues.Utype.Crew || UnitSize == Constantvalues.Utype.HalfSquad) && Firingunit.getFiringunit().getHasMG()) {
                    // need to test if Unit is using MG before applying modifier
                    // ''for ( CheckFire As Objectvalues.PersUniti In FireGroupToUse
                    //    ''    if CheckFire.Owner = FiringUnit.OBLink Then
                    Firingunit.getFiringunit().MGModification(); // HS and crew can// t fire MG and use inherent FP
                    //    ''    Exit For
                    //    ''End if
                    //    ''    Next
                }
                // crest fire modification (no FP if behind wall; 1/2 FP if out of Crest CA) - behind wall will show NOLOS unless other units in location have LOS so need to set FP to zero
                if (Firingunit.getbaseunit().IsInCrestStatus()) {
                    Firingunit.getFiringunit().CrestStatusModification(targethex);
                }
            } else {    // Firer is ineligible
                    switch (FirerStatus) {
                        case PrepFirer:
                            msg = " has Prep Fired. Can't fire again.";
                            break;
                        case AdvFirer:
                            msg = " has already Advance Fired";
                            break;
                        case FirstFirer:
                            FinalFirer:
                            msg = " is on the other side buddy!";
                            break;
                        default:
                            //msg = " is Broken";
                    }
                    // addd OrderStatus switch to handle broken - can't be part of firer status switch
                    GameModule.getGameModule().getChatter().send(Firingunit.getbaseunit().getUnitName() + msg + " No FP Added");
            }
        }
    }
    private void GetCombatFP(SuppWeapi FiringSuppW, PersUniti TargetUnit, double TotalTargetlevel, int range, int FGSize, Constantvalues.Utype UnitSize, boolean UsingSprayFire) {
        int UnitRange; double RangeFactor = 1;
        Constantvalues.CombatStatus FirerStatus; // holds status value of firing unit (inf or mg)
        int BaseFP;   // holds LOB FP value of firing unit (inf or mg)
        LevelChecks LevelChk = new LevelChecks();
        double leveldifference = 0; double TotalFirerLevel = 0; int targethex = 0;
        double UseAsRange = 0; String msg = "";
        //GetALocationFromMap Getlocs = new GetALocationFromMap(Mapcol);
        //GameLocation targloc = Getlocs.RetrieveLocationfromHex(TargetUnit.getbaseunit().getLOCIndex());
        // determine unit level
        double Baselevel = LevelChk.GetLevelofLocation(); // use location=hexnumber always tests base location in hex
        TotalFirerLevel = FiringSuppW.getbaseSW().getLevelinHex() + Baselevel;
        leveldifference = TotalFirerLevel - TotalTargetlevel;
        // determine base FP and range of unit, and if Assault Fire is possible
        UnitRange = FiringSuppW.getFiringSW().getBaseRange();
        BaseFP = FiringSuppW.getFiringSW().getBaseFP();
        // assign unit status
        FirerStatus = FiringSuppW.getFiringSW().getCombatStatus();
        if (UnitRange > 0) {   // UnitSize < constantvalues.Utype.SMC or  Then  'Unitsize is 0 when Firer is mg
            // checks for broken units and MG - this should not be necessary as
            // AddFirerUnit routine should not accept broken units
            // NEED TO FIX above JULY 2010
            //'if FirerStatus < constantvalues.OrderStatus.Brokenunit Or
            //'    (FirerStatus >= constantvalues.CombatStatus.Firing And
            //'     FirerStatus <> constantvalues.SWStatus.Brokendown And FirerStatus <> constantvalues.SWStatus.Dis_Broken) Then
            if (FirerStatus == Constantvalues.CombatStatus.Firing || FirerStatus == Constantvalues.CombatStatus.None) {
                // Start FP calc with base firepower
                // 'TempFP = BaseFP
                // Determine combat range
                PersUniti FiringUnit=null;
                int FiringUnitOwner= FiringSuppW.getbaseSW().getOwner();
                for (PersUniti FindFirer: Scencolls.Unitcol) {
                    if (FindFirer.getbaseunit().getUnit_ID() == FiringUnitOwner) {
                        FiringUnit = FindFirer;
                        break;
                    }
                }
                Hex FiringHex = scen.getGameMap().getHex(FiringUnit.getbaseunit().getHexName());
                Hex TargetHex = scen.getGameMap().getHex(TargetUnit.getbaseunit().getHexName());
                range = scen.getGameMap().range(FiringHex, TargetHex, scen.getGameMap().getMapConfiguration());
                if (range > 0) {
                    UseAsRange = range;
                } else { // same hex combat
                    UseAsRange = Math.abs(TargetUnit.getbaseunit().getLevelinHex() - FiringSuppW.getbaseSW().getLevelinHex());
                }
                // Set FiringUnit FP based on range impacts
                FiringSuppW.getFiringSW().RangeModification(UseAsRange, leveldifference, TargetUnit);
                // at this stage, after each sub TempFP=FiringUnit.CombatFP
                // Area Fire
                FiringSuppW.getFiringSW().AreaFireModification(FGSize, TargetUnit.getbaseunit().gethexlocation());
                // Pinned
                FiringSuppW.getFiringSW().PinnedModification();
                // Adv Fire
                FiringSuppW.getFiringSW().AdvancingFireModification(Scendet.getPhase());
                // Target Concealed
                FiringSuppW.getFiringSW().FireVsConcealedModification(TargetUnit);
                // First firer
                FiringSuppW.getFiringSW().FirstFireModification();
                // Spray Fire
                FiringSuppW.getFiringSW().SprayFireModification(UsingSprayFire);
                // Assault Fire check
                //Firingunit.getFiringunit().AssaultFireModification(Scendet.getPhase());
                if (FiringSuppW.getFiringSW().getUseHeroOrLeader() == Constantvalues.Utype.Leader) {
                    FiringSuppW.getFiringSW().ResetCombatFP();
                    double Forcerecalc = FiringSuppW.getFiringSW().getCombatFP();
                } else {
                    //GameForm.UpdateListBoxItem(GameForm.lbFP, i, Str(FiringUnit.CombatFP))
                }
                //if ((UnitSize == Constantvalues.Utype.Crew || UnitSize == Constantvalues.Utype.HalfSquad) && Firingunit.getFiringunit().getHasMG()) {
                    // need to test if Unit is using MG before applying modifier
                    // ''for ( CheckFire As Objectvalues.PersUniti In FireGroupToUse
                    //    ''    if CheckFire.Owner = FiringUnit.OBLink Then
                //    Firingunit.getFiringunit().MGModification(); // HS and crew can// t fire MG and use inherent FP
                    //    ''    Exit For
                    //    ''End if
                    //    ''    Next
                //}

                // crest fire modification (no FP if behind wall; 1/2 FP if out of Crest CA) - behind wall will show NOLOS unless other units in location have LOS so need to set FP to zero
                if (FiringSuppW.getbaseSW().IsInCrestStatus()) {
                    FiringSuppW.getFiringSW().CrestStatusModification(targethex);
                }
                } else {    // Firer is ineligible
                    switch (FirerStatus) {
                        case PrepFirer:
                            msg = " has Prep Fired. Can't fire again.";
                            break;
                        case AdvFirer:
                            msg = " has already Advance Fired";
                            break;
                        case FirstFirer:
                            FinalFirer:
                            msg = " is on the other side buddy!";
                            break;
                        default:
                            msg = " is Broken";
                    }
                    GameModule.getGameModule().getChatter().send(FiringSuppW.getbaseSW().getUnitName() + msg + " No FP Added");

            }
        }
    }

    private int CombatDRM(boolean alreadymoved, int range, PersUniti TargetUnit, int UsingSol) {
        // called by IFT.CalcCombatFPandDRM
        // for each hex in the chain, choose hex role and process accordingly
        // Get terrain modifiers, then hexside modifiers, then scenario feature modifiers
        // Decide which ones take precedence (in part depends on role)
        // add to display and overall calculation

        // create required variables
        int FinalCombatDRM=0;
        //boolean Terraintest; boolean HexSideTest;
        int Featuredrm = 0; // holds value of applicable drm based on scenario feature
        int Hexsidedrm = 0; // holds value of applicable hexside drm
        int Smokedrm =0;
        //int  hexvalue = 0; // holds ID value of currenthex
        //boolean  ScenFeatureTest = false;
        //int  TEMdrm; // holds value of target terrain TEM
        //int  LOSHdrm;
        String LOSHName = "";
        //double FirerBaseLevel = 0;   // used in determining height advantage
        //double FirerInHexLevel = 0 ;  // used in determining height advantage
        //double HexBaselevel = 0;
        Constantvalues.Hexside  Hexsidetype = Constantvalues.Hexside.NoTerrain;
        Hex lasthex= null; int Lasthexloshdrm = 0;  // ': Dim Lastlocindex As Integer = 0 'holds value of last hex checked and its LOSH drm \ LocIndex
        int  TotalhexDRM = 0; int TotalFireDRM = 0;
        String TerrainName = ""; String SideTerrainName = "";      // these two lines hold text describing
        String hexdrmstring = "";
        String FeatureName = ""; // the terrain element adding a drm
        int  Mistdrm = 0;
        boolean  LOSAlongHexside = false;
        //int  Firertest = 0;
        //int[]  AHGChecks; String UseAltName; String ReportName;
        boolean  MistIsLOSH = false;  // determines is mist/dust applies as LOSH rather than LV
        int  DustLOSH = 0 ;       // holds part of Mistdrm which applies as LOSH
        int TotalLocationLOSHdrm  = 0; int TotalLOSLOSHdrm = 0;    // cumulative LOSH; if =>6, LOS is blocked
        //int  TargetLOSH = 0 ;  //  holds value of LOSH in target hex which need to be part of TotalLOSHdrm calculation
        //String TargLOSHName = "";
        //String VisLOSHName = ""; // holds name of LOSH in target hex and intervening hex
        double TargetTotalLevel = 0;
        int Targethexdrm = 0;
        boolean  OBAAlreadyFound = false;
        int  FinalLOSHDrm  = 0;
        //int FinalFeatureDRM = 0;
        int FinalVisLOSH = 0;
        //String FinalVisLOSHName = "";
        int VisLOSH = 0; String FinalLOSHName = "";
        //CombatTerrain UsingComTer;
        LinkedList<IFTMods> DRMList = new LinkedList<IFTMods>();
        //int  TestDRM  = 0;
        //LinkedList<IFTMods> Removelist = new LinkedList<IFTMods>();
        int  HexSpineDRM = 0; // holds value of drm of hexspine when LOS follows that hexspine and it connects to a target hex vertex
        IFTMods NEWDrm;


        // Terrain-based DRM
        // Adding Mist and Dust which are range-based not hex-based
        // NOTE: need to incorporate dust into this routine
        Constantvalues.Mist Mistvalue= Constantvalues.Mist.None;  //  temporary while debugging UNDO
        Constantvalues.Dust Dustvalue = Constantvalues.Dust.None; // need to seperate mist and dust in Scendet.MistDust
        TerrainChecks TerrChk = new TerrainChecks();   // class for various data-based terrain checks
        ThreadedLOSCheckCommonc ThreadedCommonMethods = new ThreadedLOSCheckCommonc(TerrChk);
        Mistdrm = ThreadedCommonMethods.MistDustmodifier(range, MistIsLOSH, DustLOSH, Mistvalue, Dustvalue);
        if (Mistdrm == -1) {
            GameModule.getGameModule().getChatter().send("Firing Units cannot see target: Visibility Blocked by Mist");
            return 99;  // LOS blocked
        }
        if (MistIsLOSH) {TotalLocationLOSHdrm = (DustLOSH > 0 ? DustLOSH: Mistdrm);}  //SHOULD BE TOTALLOSLOSHDRM??

        for (LOSSolution ValidSol: ValidSolutions) {
            FinalCombatDRM = 0; TotalLOSLOSHdrm = 0;
            if ((UsingSol == -1 || UsingSol == ValidSol.getID()) && TargetUnit.getbaseunit().getHex().getName() == ValidSol.getSeenHex().getName()) {   // -1 forces use of all valid solutions - needed in actual fire resolution for multi-hex fire group; if not -1 then checking hex by hex as in DFFMVCPattern
                if (ValidSol.getLOSFollows() == Constantvalues.LOS.AltHexGrain || ValidSol.getLOSFollows() == Constantvalues.LOS.HorizontalHexGrain || ValidSol.getLOSFollows() == Constantvalues.LOS.Is60) {
                    TargetTotalLevel = ValidSol.getTotalSeenLevel();
                    LOSAlongHexside = true;
                }

                for (CombatTerrain ComTer : ValidSol.getHexesInLOS()) {
                    if (ComTer.getSolID() != ValidSol.getID()) {
                        continue;
                    }
                    // initialize variables
                    Targethexdrm=0; // needs to be reset or may be added for each hex, which is wrong
                    // Terrain Modifiers
                    // determine what hex is being checked: firer, intervening or target
                    // can be more than one at the same time
                    // functions set values within ComTer and other values in this routine
                    if (ComTer.IsFirer()) {
                        //HexBaselevel = ComTer.getHexBaseLevel();
                        lasthex = null;
                    }
                    //if (HexBaselevel != FirerBaseLevel) {
                    //    FirerBaseLevel = HexBaselevel;
                   // }
                    TargetVariables targetvar = new TargetVariables();
                    if (ComTer.IsTarget() && ComTer.getHexName() == TargetUnit.getbaseunit().getHex().getName()) {
                        ComTer.SetTargetVariables(targetvar, TargetUnit, lasthex, TerrainName, Hexsidetype, ValidSol.getLOSFollows(), ValidSol.getSeeHex());
                    }
                    if (TerrainName == "") {
                        TerrainName = ComTer.gethexdesc();
                    }
                    lasthex = ComTer.getLocation().getHex();

                    // Hexside modifiers  - THIS SHOULD ONLY APPLY IF COMTER.ISTARGET ???
                    Hexsidedrm=0;
                    if (targetvar.getHexSideTest()) {
                        /*Hexsidedrm = ComTer.getHexsideCrossedTEM();
                        SideTerrainName = ComTer.getHexsideCrosseddesc();
                        if (Hexsidedrm < 1) {  // hexsideTEM = 0 then no terrain
                            //HexSideTest = false;
                            Hexsidetype = Constantvalues.Hexside.NoTerrain;
                            SideTerrainName = "";
                        }*/
                    }
                    // Featurename is passed ByRef so starts as "" but returns as actual string - NEED TO TEST THIS IN JAVA
                    Featuredrm = ComTer.GetScenFeatTEM(FeatureName);
                    // Now decide which drms to use
                    // use of highest drm is not automatic and players will have made choices to use Wall Adv (for example) or not;
                    // need to access these choices in further development
                    // plus some Scenario features are cumulative (smoke) and some are not (pillbox or foxhole); this needs to be added
                    // check hexrole separately as both can apply

                    // Firer
                    // put these out as methods
                    if (ComTer.IsFirer()) {
                        // need to check for visibility drm (SMOKE, OBA)
                        TotalLocationLOSHdrm = ComTer.FiringDRM(DRMList, TargetUnit);
                        TotalLOSLOSHdrm += TotalLocationLOSHdrm;
                    }
                    // Intervening
                    if (ComTer.IsIntervening()) {
                        TotalLocationLOSHdrm = ComTer.InterveningDRM(DRMList, TargetUnit);
                        TotalLOSLOSHdrm += TotalLocationLOSHdrm;
                    }
                    if (ComTer.IsTarget() && ComTer.getHexName() == TargetUnit.getbaseunit().getHex().getName()) {
                        // now add visibility losh
                        TotalLocationLOSHdrm = ComTer.getTargetLOSHdrm(DRMList, TargetUnit); //targetvar.getLOSHdrm();
                        //targetvar.setLOSHdrm(ComTer.CalcVisLOSH());
                        // now add Terrain drm and/or other conditions in target hex
                        Targethexdrm = ComTer.TargetHexdrm(targetvar, DRMList, TargetUnit, HexSpineDRM, alreadymoved, FireGroupToUse);
                        //TotalLocationLOSHdrm = targetvar.getLOSHdrm();
                        TotalLOSLOSHdrm += TotalLocationLOSHdrm;
                     }
                    if (TotalLOSLOSHdrm >= 6) {
                        GameModule.getGameModule().getChatter().send("LOSH is Blocked in " + ComTer.getHexName());
                        return 99;
                    }
    /*
                            'if Not (Featuredrm = 0 And TargetLOSH > 0) Then
                                    '    TargLOSHName = ""
                                    '    TargetLOSH = 0
                                    'End if
                                    'the purpose of the above line is to use smoke or other LOSH in target hex in addition
                                    'to Terrain or hexside TEM; already checked if total LOSH blocks LOS
                                    'now determine and paste results to results box*/
                    TotalhexDRM = Targethexdrm + TotalLocationLOSHdrm + Hexsidedrm + Featuredrm;  // need to add others NOT SURE
                    hexdrmstring = TerrainName + LOSHName + FeatureName + SideTerrainName + targetvar.getLOSHName();
                    if (TotalhexDRM != 0) {
                        //ReportName = (targetvar.getUseAltName() == "" ? ComTer.getHexName(): targetvar.getUseAltName());
                        //        ''GameForm.GridAddRows(GameForm.grdDRMMOd, ReportName,  "Total: " & hexdrmstring, "", CStr(TotalhexDRM))
                    }
                    FinalCombatDRM += TotalhexDRM;
                }  // next hex along los
            } // next LOS solution
            TotalFireDRM += FinalCombatDRM;

                    // Move this elsewhere when dealing with multihex FG
/*
                            'if ValidSol.ID = 1 Then 'first time through so accept values
                    '    for ( AddDRM As IFTMods In DRMList
                            '        FinalDRMLIst.Add(AddDRM)
                            '    Next
                            '    TestDRM = CombatDRM
                            'ElseIf CombatDRM >= TestDRM Then 'need to compare drm values for different solutions.
            '    for ( DelDRM As IFTMods In FinalDRMLIst
            '        Select Case DelDRM.DRMType
            '            Case IFTdrm.SMC, IFTdrm.Hero, IFTdrm.FirerCX, IFTdrm.FirerEnc
            '            Case Else
            '                Removelist.Add(DelDRM)
            '        End Select
            '    Next
            '    for ( DelDrm As IFTMods In Removelist
            '        FinalDRMLIst.Remove(DelDrm)
            '    Next
            '    for ( AddDRM As IFTMods In DRMList
            '        FinalDRMLIst.Add(AddDRM)
            '    Next
            '    TestDRM = CombatDRM
            'End if
            'DRMList.Clear()*/
        }
        for (IFTMods AddDRM: DRMList) {
            HoldTerrainDRMList.add(AddDRM);
        }
        return (TotalFireDRM + Mistdrm);  // Mist added on a LOS range basis not per hex

    }

    private double DoFPCalc(PersUniti TargetUnit, int Usingsol, LinkedList<PersUniti> FireGroupToUse, boolean UsingSprayFire, int FGSize) {
        int FirerStatus; // holds status value of firing unit (inf or mg)
        int BaseFP;// holds LOB FP value of firing unit (inf or mg)
        Constantvalues.Utype UnitSize = Constantvalues.Utype.None;
        int Unitrange = 0;
        double UseAsRange = 0;  int range = 0;
        double leveldifference = 0;
        double TotalTargetLevel = 0; double TotalFirerLevel = 0;
        LOSSolution ValidSol= null;
        CombatTerrain TargetComTer=null;
        String msg = "";

        for (LOSSolution CheckSol: ValidSolutions) {
            if (CheckSol.getID() == Usingsol) {
                ValidSol = CheckSol;
                break;
            }
        }
        double TotalFP  = 0;
        for (CombatTerrain CheckComTer: ValidSol.getHexesInLOS()) {
            if (CheckComTer.getLocation().equals(TargetUnit.getbaseunit().gethexlocation()) && CheckComTer.getSolID() == Usingsol) {
                TargetComTer = CheckComTer;
                break;
            }
        }
        TotalTargetLevel = TargetUnit.getbaseunit().getLevelinHex() + TargetComTer.getHexBaseLevel();
        //'targloc = Getlocs.RetrieveLocationfromHex(CInt(TargetUnit.BasePersUnit.LOCIndex))

        for (PersUniti FiringUnit: FireGroupToUse) {
            if (FiringUnit.getbaseunit().getSolID() == Usingsol) {
                UnitSize = FiringUnit.getbaseunit().getUnittype();
                if (FiringUnit.getFiringunit().getUsingInherentFP()) {
                    // need to reset CombatFP and TempFP to zero each time or becomes additive
                    FiringUnit.getFiringunit().ResetCombatFP();
                    GetCombatFP(FiringUnit, TargetUnit, TotalTargetLevel, range, FGSize, UnitSize, UsingSprayFire);
                    TotalFP += FiringUnit.getFiringunit().getCombatFP();
                }
                SuppWeapi FiringMG=null;
                if (FiringUnit.getFiringunit().getUsingfirstMG()) {
                    for (SuppWeapi CheckMG: FiringUnit.getFiringunit().FiringMGs) {
                        if (CheckMG.getbaseSW().getSW_ID()== FiringUnit.getbaseunit().getFirstSWLink());
                            FiringMG=CheckMG;
                            break;
                    }
                    FiringMG.getFiringSW().ResetCombatFP();
                    GetCombatFP(FiringMG, TargetUnit, TotalTargetLevel, range, FGSize, UnitSize, UsingSprayFire);
                    TotalFP += FiringMG.getFiringSW().getCombatFP();
                }
                if (FiringUnit.getFiringunit().getUsingsecondMG()) {
                    for (SuppWeapi CheckMG: FiringUnit.getFiringunit().FiringMGs) {
                        if (CheckMG.getbaseSW().getSW_ID()== FiringUnit.getbaseunit().getSecondSWLink());
                        FiringMG=CheckMG;
                        break;
                    }
                    FiringMG.getFiringSW().ResetCombatFP();
                    GetCombatFP(FiringMG, TargetUnit, TotalTargetLevel, range, FGSize, UnitSize, UsingSprayFire);
                    TotalFP += FiringMG.getFiringSW().getCombatFP();
                }
            }
        }
        return TotalFP;
    }

    private FiringDRMs CalcFiringDRMs(boolean UsingSprayFire, int Usingsol) {
        FiringDRMs firingdrms = new FiringDRMs();
        int FGSize = 0; int mgrange=0; int range = 0;
        boolean CXTest = false; boolean EncircTest = false;
        Constantvalues.Utype UnitSize = Constantvalues.Utype.None;
        for (PersUniti FiringUnit: FireGroupToUse) {
            // CX applies to all units in FG so if found once no need to check again  - if then below does not cover all possibilities
            if (!CXTest) {
                if (FiringUnit.getFiringunit().getIsCX()) {
                    CXTest = true;
                    firingdrms.setCXapplies(true);
                    firingdrms.setCXHex(FiringUnit.getbaseunit().getHexName());
                    firingdrms.setCXName(FiringUnit.getbaseunit().getUnitName());
                }
            }
            // Encirclement
            // if then below does not cover all possibilities
            // Encirclement applies to all units in FG so if found once no need to check again
            if (!EncircTest) {
                if (FiringUnit.getFiringunit().getIsEncirc()) {
                    EncircTest = true;
                    firingdrms.setEncircapplies(true);
                    firingdrms.setEncircHex(FiringUnit.getbaseunit().getHexName());
                    firingdrms.setEncircName(FiringUnit.getbaseunit().getUnitName());
                }
            }
            // Hero impact
            UnitSize = FiringUnit.getbaseunit().getUnittype();
            if (UnitSize == Constantvalues.Utype.Hero ||
                    (UnitSize == Constantvalues.Utype.LdrHero && FiringUnit.getFiringunit().getUseHeroOrLeader() != Constantvalues.Utype.Leader)) {
                // Heroes are cumulative; always add if in range, unless using ldr drm instead
                // get combat range
                range = DetermineMaxCombatRange(FiringUnit, TargetGroupToUse, UsingSprayFire);
                // determine hero range (base range or SW range)
                boolean UsingMGRange= false; SuppWeapi MGRange; // = 0;
                if (FiringUnit.getFiringunit().getUsingfirstMG()) { // use MG range if using
                    mgrange = FiringUnit.getFiringunit().FiringMGs.getFirst().getFiringSW().getBaseRange();
                    UsingMGRange = true;
                }
                if (FiringUnit.getFiringunit().getUsingsecondMG()) { // use MG range if using
                    mgrange = FiringUnit.getFiringunit().FiringMGs.getLast().getFiringSW().getBaseRange();
                    UsingMGRange = true;
                }
                if (UsingMGRange) { // use MG base range
                    if (mgrange >= range) {
                        firingdrms.setHeroicdrm(firingdrms.getHeroicdrm() + -1);
                        firingdrms.setHeroName(firingdrms.getHeroName() + " " + FiringUnit.getbaseunit().getUnitName());
                    }
                } else {//  use hero base range
                    if (FiringUnit.getFiringunit().getBaseRange() >= range) {
                        firingdrms.setHeroicdrm(firingdrms.getHeroicdrm() + -1);
                        firingdrms.setHeroName(firingdrms.getHeroName() + " " + FiringUnit.getbaseunit().getUnitName());
                    }
                }
            }
            if (UnitSize != Constantvalues.Utype.Leader && !(UnitSize == Constantvalues.Utype.LdrHero && FiringUnit.getFiringunit().getUseHeroOrLeader() == Constantvalues.Utype.Leader)) {
                if (UnitSize == Constantvalues.Utype.Squad) {
                    firingdrms.setFGSize(firingdrms.getFGSize() +3);
                } else if (UnitSize == Constantvalues.Utype.HalfSquad || UnitSize == Constantvalues.Utype.Crew) {
                    firingdrms.setFGSize(firingdrms.getFGSize() +2);
                } else {
                    firingdrms.setFGSize(firingdrms.getFGSize() +1);
                }
            }
            // now add SMC impact
            boolean Ldrpresent = false;
             int LdrMod = CombatLdrDRM(FireGroupToUse, Ldrpresent, Usingsol, firingdrms);
        }
        return firingdrms;
    }
    private int VehicleTEMCheck(LinkedList<IFTMods> DRMList, PersUniti TargetUnit, CombatTerrain ComTer ){
        boolean PositiveDRM = false;
        for (IFTMods IFTdrmTest: DRMList) {
            if (IFTdrmTest.getDRM() > 0 && IFTdrmTest.getDRMLocation().equals(ComTer.getLocation())) {
                PositiveDRM = true;
                break;
            }
        }
        if (!PositiveDRM && AFVIsPresent(ComTer)) {
            if (ComTer.NotAlreadyAddedToDRMList(DRMList, TargetUnit, Constantvalues.IFTdrm.VehWrkTEM)) {
                IFTMods NewDRM = new IFTMods(1, Constantvalues.IFTdrm.VehWrkTEM, TargetUnit.getbaseunit().getUnit_ID(), TargetUnit.getbaseunit().getTypeType_ID(), TargetUnit.getbaseunit().gethexlocation(), ComTer.getLocation(), "VehWrkTEM");
                DRMList.add(NewDRM);
            }
            return 1;
        }
        return 0;
    }

    private boolean AFVIsPresent(CombatTerrain ComTer) {
        // temporary while debugging UNDO
        /*LinkedList<AFV> VehLIst = new LinkedList<AFV>();
        // Get Vehicle if exists
        try {
            VehLIst = (Linqdata.GetVehiclesInHex(ComTer.getHexID())).ToList;
        } catch(Exception e) {
            return false; // no vehicles found in hex
        }
        if (VehLIst.size() == 0) {return false;} //  no vehicles found
        // check veh type
        for (AFV Selvehicle: VehLIst){
            // is afv
            if (!Selvehicle.ISAFV()) {continue;}
            // veh status
            if (Selvehicle.VehicleStatus == Constantvalues.Vmove.Motion || Selvehicle.VehicleStatus == Constantvalues.Vmove.Moving || Selvehicle.VehicleStatus == Constantvalues.Vmove.Moved) {continue;}
            if (Selvehicle.VehicleStatus == Constantvalues.VStatus.Burning) {continue;}
                'if Selvehicle.levelinhex <> ComTer.HexBaseLevel then Continue for
            // if Selvheicle entrenched or dug in - NEED TO PROGRAM
            // if find one valid vehicle then return true
            return true;
        }*/
        return false; // no valid vehicle present
    }

    private boolean AFVInLOS(CombatTerrain comter ) {
        // NEEDS TO BE PROGRAMMED
        return true;
    }

    private boolean RangeIsEqual(int Currenthex, int lasthex, int Starthex) {
        /*Dim MapGeo as mapgeovalues.mapgeoc = MapGeovalues.MapGeoC.GetInstance(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0) // can use null values if sure instance already exists
        Dim Firstrange As Integer = MapGeo.CalcRange(Starthex, Currenthex, true)
        Dim Secondrange As Integer = MapGeo.CalcRange(lasthex, Currenthex, true)
        if Firstrange = Secondrange Then return true Else return false*/
        // temporary while debugging UNDO
        return false;
    }
    public class TargetVariables{
        private boolean pTerraintest = false;
        private boolean pHexSideTest = false;
        private int phexvalue = 0;
        private int pFeaturedrm = 0;
        private String pFeatureName = "";
        private boolean pScenFeatureTest = false;
        private int pTEMdrm = 0;
        private int pHexsidedrm = 0;
        private int pLOSHdrm = 0;
        private String pUseAltName = "";
        private String pTerrainName = "";
        private int pTotalLocationLOSHdrm = 0;
        private String pLOSHName = ""; // need to reset it here because not reset later
        private String pHexSideName ="";


        private TargetVariables(){ }

        public boolean getHexSideTest() {return pHexSideTest;}
        public void setHexSideTest(boolean value) {pHexSideTest=value;}
        public boolean getTerraintest() {return pTerraintest;}
        public void setTerraintest(boolean value) {pTerraintest=value;}
        public int getHexvalue() {return phexvalue;}
        public void setHexvalue(int value){phexvalue = value;}
        public int getFeaturedrm() {return pFeaturedrm;}
        public void setFeaturedrm(int value){pFeaturedrm=value;}
        public String getFeatureName() {return pFeatureName;}
        public void setFeatureName(String value){pFeatureName=value;}
        public boolean getScenFeatureTest() {return pScenFeatureTest;}
        public void setScenFeatureTest(boolean value) {pScenFeatureTest=value;}
        public int getTEMdrm() {return pTEMdrm;}
        public void setTEMdrm(int value){pTEMdrm=value;}
        public int getHexsidedrm() {return pHexsidedrm;}
        public void setHexsidedrm(int value){pHexsidedrm=value;}
        public int getLOSHdrm(){return pLOSHdrm;}
        public void setLOSHdrm(int value){pLOSHdrm=value;}
        public String getUseAltName() {return pUseAltName;}
        public void setUseAltName(String value) {pUseAltName=value;}
        public String getTerrainName() {return pTerrainName;}
        public void setTerrainName(String value){pTerrainName=value;}
        public int getTotalLocationLOSHdrm() {return pTotalLocationLOSHdrm;}
        public void setTotalLocationLOHSdrm(int value) {pTotalLocationLOSHdrm=value;}
        public String getLOSHName() {return pLOSHName;}
        public void setLOSHName(String value){pLOSHName=value;}
        public String getHexsidedesc(){return pHexSideName; }
        public void setHexsidedesc(String value){pHexSideName = value;}
    }
    private class FiringDRMs{
        private boolean pCXApplies = false;
        private boolean pEncircApplies = false;
        private int pHeroicdrm =0;
        private int pLeaderdrm = 0;
        private String pCXHex = "";
        private String pCXName ="";
        private String pEncircHex="";
        private String pEncircName="";
        private String pHeroName="";
        private int pFGSize=0;
        private String pLeaderName="";

        public boolean getCXapplies() {return pCXApplies;}
        public void setCXapplies(boolean value) {pCXApplies = value;}
        public boolean getEncircapplies() {return pEncircApplies;}
        public void setEncircapplies(boolean value) {pEncircApplies = value;}
        public int getHeroicdrm() {return pHeroicdrm;}
        public void setHeroicdrm(int value){pHeroicdrm=value;}
        public int getLeaderdrm() {return pLeaderdrm;}
        public void setLeaderdrm(int value){pLeaderdrm=value;}
        public String getCXHex(){return pCXHex;}
        public void setCXHex(String value){pCXHex = value;}
        public String getCXName(){return pCXName;}
        public void setCXName(String value){pCXName = value;}
        public String getEncircHex (){return pEncircHex;}
        public void setEncircHex(String value){pEncircHex=value;}
        public String getEncircName(){return pEncircName;}
        public void setEncircName(String value){pEncircName = value;}
        public String getHeroName(){return pHeroName;}
        public void setHeroName(String value){pHeroName=value;}
        public int getFGSize(){return pFGSize;}
        public void setFGSize(int value){pFGSize = value;}
        public String getLeaderName(){return pLeaderName;}
        public void setLeaderName(String value){pLeaderName = value;}
    }

}
