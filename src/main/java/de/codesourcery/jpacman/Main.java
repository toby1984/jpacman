package de.codesourcery.jpacman;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Main extends JFrame
{
    public static void main(String[] args) throws InvocationTargetException, InterruptedException
    {
        SwingUtilities.invokeAndWait(() ->
        {
            try
            {
                new Main().run();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        });
    }

    public void run() throws IOException
    {
        final GameState state = new GameState();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setLayout(new GridBagLayout());
        final GridBagConstraints cnstrs = new GridBagConstraints();
        cnstrs.weightx=1.0;
        cnstrs.weighty=1.0;
        cnstrs.fill=GridBagConstraints.BOTH;
//        getContentPane().add( new EditorPanel() , cnstrs );

        final PlayingField panel = new PlayingField(state);
        getContentPane().add(panel, cnstrs );
        setLocationRelativeTo(null);
        pack();
        setVisible(true);

        final Timer t = new Timer(16, new ActionListener()
        {
            private long lastTick;
            @Override
            public void actionPerformed(ActionEvent ev)
            {
                final long now = System.currentTimeMillis();
                if ( lastTick != 0 )
                {
                    float elapsedSeconds = (now - lastTick ) / 1000f;
                    state.tick(panel.userInput,elapsedSeconds);
                } else {
                    lastTick = now;
                }
                panel.repaint();
            }
        });
        t.start();
    }
}
