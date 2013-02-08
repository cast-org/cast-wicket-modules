package org.cast.cwm;

import org.apache.wicket.markup.html.SecurePackageResourceGuard;

public class CwmPackageResourceGuard extends SecurePackageResourceGuard {

        public CwmPackageResourceGuard() {
                super(new SimpleCache(100));
                addPattern("+*.pdf");
                addPattern("+**/mediaplayer/skins/**/*.xml");
                addPattern("+*.htm");
                addPattern("+*.csv");
        }
        
}

