package lcmc.gui.resources.drbd;

import java.util.List;

import lcmc.model.AccessMode;
import lcmc.model.resources.BlockDevice;
import lcmc.gui.HostBrowser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import lcmc.utilities.EnablePredicate;
import lcmc.utilities.MenuAction;
import lcmc.utilities.MenuFactory;
import lcmc.utilities.MyMenu;
import lcmc.utilities.MyMenuItem;
import lcmc.utilities.Predicate;
import lcmc.utilities.UpdatableItem;
import lcmc.utilities.VisiblePredicate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.runners.MockitoJUnitRunner;

import javax.swing.ImageIcon;

@RunWith(MockitoJUnitRunner.class)
public class BlockDevMenuITest {
    @Mock
    private BlockDevInfo blockDevInfoStub;
    @Mock
    private BlockDevice blockDeviceStub;
    @Mock
    private HostBrowser hostBrowserStub;
    @Mock
    private BlockDevInfo blockDevInfoNoClusterStub;
    @Mock
    private HostBrowser hostBrowserNoClusterStub;
    @Mock
    private MyMenu menuStub;
    @Mock
    private MyMenuItem menuItemStub;
    @Mock
    private MenuFactory menuFactoryStub;
    @InjectMocks
    private BlockDevMenu blockDevMenu;
    @InjectMocks
    private BlockDevMenu blockDevMenuNoCluster;

    @Before
    public void setUp() {
        when(blockDevInfoStub.getBrowser()).thenReturn(hostBrowserStub);
        when(blockDevInfoStub.getBlockDevice()).thenReturn(blockDeviceStub);

        when(blockDevInfoNoClusterStub.getBlockDevice()).thenReturn(blockDeviceStub);
        when(menuFactoryStub.createMenu(
                anyString(),
                (AccessMode) anyObject(),
                (AccessMode) anyObject())).thenReturn(menuStub);
        when(menuFactoryStub.createMenuItem(
                anyString(),
                (ImageIcon) anyObject(),
                anyString(),
                (AccessMode) anyObject(),
                (AccessMode) anyObject())).thenReturn(menuItemStub);
        when(menuFactoryStub.createMenuItem(anyString(),
                (ImageIcon) anyObject(),
                anyString(),

                anyString(),
                (ImageIcon) anyObject(),
                anyString(),

                (AccessMode) anyObject(),
                (AccessMode) anyObject())).thenReturn(menuItemStub);
        when(menuStub.enablePredicate((EnablePredicate) anyObject())).thenReturn(menuStub);
        when(menuItemStub.enablePredicate((EnablePredicate) anyObject())).thenReturn(menuItemStub);
        when(menuItemStub.predicate((Predicate) anyObject())).thenReturn(menuItemStub);
        when(menuItemStub.visiblePredicate((VisiblePredicate) anyObject())).thenReturn(menuItemStub);
        when(menuItemStub.addAction((MenuAction) anyObject())).thenReturn(menuItemStub);
    }

    @Test
    public void menuShouldHaveItems() {
        final List<UpdatableItem> items = blockDevMenu.getPulldownMenu(blockDevInfoStub);

        verify(menuItemStub, times(6)).predicate((Predicate) anyObject());
        verify(menuItemStub, times(19)).visiblePredicate((VisiblePredicate) anyObject());
        verify(menuItemStub, times(19)).enablePredicate((EnablePredicate) anyObject());
        verify(menuItemStub, times(19)).addAction((MenuAction) anyObject());
        verify(menuStub, times(1)).enablePredicate((EnablePredicate) anyObject());
        verify(menuStub, times(1)).onUpdate((Runnable) anyObject());
        assertEquals(20, items.size());
    }

    @Test
    public void menuWithOrWithoutClusterShoulBeTheSameSize() {
        final List<UpdatableItem> itemsWithCluster = blockDevMenu.getPulldownMenu(blockDevInfoStub);
        when(blockDevInfoNoClusterStub.getBrowser()).thenReturn(hostBrowserNoClusterStub);

        final List<UpdatableItem> itemsNoCluster = blockDevMenuNoCluster.getPulldownMenu(blockDevInfoNoClusterStub);

        assertTrue(itemsNoCluster.size() == itemsWithCluster.size());
    }
}