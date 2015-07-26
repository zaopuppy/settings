# IntelliJ-Community 


com.intellij.idea.Main#main
  com.intellij.idea.StartupUtil.AppStarter#start
    com.intellij.idea.IdeaApplication#IdeaApplication
      com.intellij.openapi.application.impl.ApplicationImpl#ApplicationImpl
    com.intellij.idea.IdeaApplication#run // app.load(PathManager.getOptionsPath());
      com.intellij.idea.IdeaApplication.IdeStarter#main {
        if (!willOpenProject.get()) {
          WelcomeFrame.showNow();
          lifecyclePublisher.welcomeScreenDisplayed();
        }
        else {
          windowManager.showFrame();
        }
      }

com.intellij.ide.actions.ViewStructureAction#actionPerformed
com.intellij.ide.util.FileStructurePopup#installUpdater

com.intellij.codeInsight.documentation.DocumentationComponent#registerActions

com.intellij.codeInsight.documentation.DocumentationComponent#DocumentationComponent(com.intellij.codeInsight.documentation.DocumentationManager, com.intellij.openapi.actionSystem.AnAction[]) {
  @Override
  protected void processKeyEvent(KeyEvent e) {
    KeyStroke keyStroke = KeyStroke.getKeyStrokeForEvent(e);
    ActionListener listener = myKeyboardActions.get(keyStroke);
    if (listener != null) {
      listener.actionPerformed(new ActionEvent(DocumentationComponent.this, 0, ""));
      e.consume();
      return;
    }
    super.processKeyEvent(e);
  }
}



