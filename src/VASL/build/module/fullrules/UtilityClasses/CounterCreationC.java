package VASL.build.module.fullrules.UtilityClasses;

import VASL.counters.ColoredBox;
import VASL.counters.Concealment;
import VASL.counters.MarkMoved;
import VASSAL.build.Configurable;
import VASSAL.build.GameModule;
import VASSAL.build.widget.PieceSlot;
import VASSAL.counters.*;
import VASSAL.tools.ComponentPathBuilder;

public class CounterCreationC {

    public CounterCreationC(){

    }
    /**
     * @return a new GamePiece that is a new counter of the desired type
     */
    public GamePiece createCounter(String countername) {
        //countername = "VASSAL.build.module.PieceWindow:VASL Counters/VASSAL.build.widget.TabWidget/VASSAL.build.widget.BoxWidget:Axis OB/VASSAL.build.widget.TabWidget:German/VASSAL.build.widget.TabWidget:SMC";
        GamePiece p = null;
        if (countername != null) {
            try {
                Configurable[] c = ComponentPathBuilder.getInstance().getPath(countername);
                if (c[c.length - 1] instanceof PieceSlot) {
                    p = PieceCloner.getInstance().clonePiece(((PieceSlot) c[c.length - 1]).getPiece());
                }
                p = GameModule.getGameModule().createPiece("<VASSAL.build.widget.PieceSlot entryName=\"Wall Advan\" gpid=\"69\" height=\"48\" width=\"48\">+/null/prototype;DBCommon\thide;72,130;HIP;;player:;0.30000001192092896\\\tlabel;76,130;Label;10;255,255,255;0,0,0;t;0;c;0;b;c;$pieceName$ ($label$);Dialog;0;0;TextLabel;\\\\\tpiece;K;D;MS\\/WallAdv;Wall Advan/\tnull\\\t\\\\\tnull;0;0;69</VASSAL.build.widget.PieceSlot>");
            }
            catch (ComponentPathBuilder.PathFormatException e) {
                boolean reg = true;
            }
        }


        /*if (p == null) {
            p = new BasicPiece(BasicPiece.ID + "K;D;" + imageName + ";?");
            boolean large = imageName.indexOf("58") > 0;
            if (!imageName.startsWith(nation)) { // Backward compatibility with generic concealment markers
                large = imageName.substring(0, 1).toUpperCase().
                        equals(imageName.substring(0, 1));
                String size = large ? "60;60" : "48;48";
                if (nation2 != null) {
                    p = new ColoredBox(ColoredBox.ID + "ru" + ";" + size, p);
                    p = new ColoredBox(ColoredBox.ID + "ge" + ";"
                            + (large ? "48;48" : "36;36"), p);
                }
                else {
                    p = new ColoredBox(ColoredBox.ID + nation + ";" + size, p);
                }
                p = new Embellishment(Embellishment.OLD_ID + ";;;;;;0;0;"
                        + imageName + ",?", p);
            }
            p = new Concealment(Concealment.ID + GameModule.getUserId() + ";" + nation, p);
            p = new MarkMoved(MarkMoved.ID + (large ? "moved58" : "moved"), p);
            p = new Hideable("hide;H;HIP;255,255,255", p);
            p = new FreeRotator("rotate;6;88,130;90,130;CA cw;CA ccw;;;", p);
        }*/

        return p;
    }
}
