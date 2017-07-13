package org.pentaho.di.ui.spoon.git;

import static org.pentaho.di.ui.spoon.git.PdiDiff.*;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.extension.ExtensionPoint;
import org.pentaho.di.core.extension.ExtensionPointInterface;
import org.pentaho.di.core.gui.GCInterface;
import org.pentaho.di.core.gui.Point;
import org.pentaho.di.core.gui.BasePainter;
import org.pentaho.di.core.gui.PrimitiveGCInterface.EImage;
import org.pentaho.di.core.logging.LogChannelInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.TransPainter;
import org.pentaho.di.ui.core.PropsUI;

@ExtensionPoint(
    id = "DrawDiffOnTransExtensionPoint",
    description = "Draws a marker on top of a step if it has some change",
    extensionPointId = "TransPainterEnd" )
public class DrawDiffOnStepExtensionPoint implements ExtensionPointInterface {

  @Override
  public void callExtensionPoint( LogChannelInterface log, Object object ) throws KettleException {
    if ( !( object instanceof TransPainter ) ) {
      return;
    }
    int iconsize = PropsUI.getInstance().getIconSize();
    TransPainter painter = (TransPainter) object;
    GCInterface gc = painter.getGc();
    TransMeta transMeta = painter.getTransMeta();
    transMeta.getSteps().stream().filter( step -> step.getAttribute( ATTR_GIT, ATTR_STATUS ) != null )
      .forEach( step -> {
        if ( transMeta.getTransversion() == null ? false : transMeta.getTransversion().startsWith( "git" ) ) {
          String status = step.getAttribute( ATTR_GIT, ATTR_STATUS );
          Point n = step.getLocation();
          EImage image = null;
          String location = "org/pentaho/di/ui/spoon/git/images/";
          if ( status.equals( REMOVED ) ) {
            location += "removed.svg";
          } else if ( status.equals( CHANGED ) ) {
            location += "changed.svg";
          } else if ( status.equals( ADDED ) ) {
            location += "added.svg";
          } else { // Unchanged
            return;
          }
          gc.drawImage( location, getClass().getClassLoader(), ( n.x + iconsize ) - ( BasePainter.MINI_ICON_SIZE / 2 ), n.y - ( BasePainter.MINI_ICON_SIZE / 2 ) );
        } else {
          step.getAttributesMap().remove( ATTR_GIT );
        }
      } );
  }
}
