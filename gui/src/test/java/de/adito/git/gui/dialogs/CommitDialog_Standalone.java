package de.adito.git.gui.dialogs;

import com.bulenkov.darcula.DarculaLaf;
import com.google.inject.*;
import com.revivius.nb.darcula.adito.AditoDarkLFCustoms;
import de.adito.git.gui.guice.AditoGitModule;
import io.reactivex.Observable;
import org.netbeans.swing.plaf.Startup;

import javax.swing.*;
import javax.swing.border.*;
import java.util.*;

/**
 * @author w.glanzer, 07.02.2019
 */
public class CommitDialog_Standalone
{

  public static void main(String[] args) throws UnsupportedLookAndFeelException
  {
    UIManager.put("Nb.DarculaLFCustoms", new AditoDarkLFCustoms());
    Startup.run(DarculaLaf.class, 12, null);


    Injector injector = Guice.createInjector(new AditoGitModule());
    IDialogFactory factory = injector.getInstance(IDialogFactory.class);

    CommitDialog dialogPane = factory.createCommitDialog(pValid -> {
    }, Observable.empty(), Observable.just(Optional.of(List.of())), "");
    JFrame dialog = new JFrame();
    dialogPane.setBorder(new CompoundBorder(new EmptyBorder(7, 7, 7, 7), dialogPane.getBorder()));
    dialog.setContentPane(dialogPane);
    dialog.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    dialog.setSize(dialogPane.getPreferredSize());
    dialog.setVisible(true);
  }

}
