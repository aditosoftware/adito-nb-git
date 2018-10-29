package de.adito.git.gui.window;

import de.adito.git.api.IRepository;
import io.reactivex.Observable;

/**
 * @author m.kaspera 29.10.2018
 */
interface IWindowFactory {

    StatusWindow createStatusWindow(Observable<IRepository> pRepository);

    BranchListWindow createBranchListWindow(Observable<IRepository> pRepository);

}
