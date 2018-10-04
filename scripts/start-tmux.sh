#!/bin/zsh

source "${0:a:h}/.common.sh"

function initiate-tmux() {
  # Initiate with TmuxSessionName
  new-session;

  MainW=$TmuxSessionName:0
  set-option $MainW allow-rename off > /dev/null;
  splitwh-setup $MainW '.' \
      'sbt' \
      'git status' \
  ;
  select-pane $MainW -L; send-to $MainW Enter;
  select-pane $MainW -R; send-to $MainW Enter;
  rename-window $MainW 'root';

  PulpJVMW=$TmuxSessionName:1
  new-window-splitwh-setup $PulpJVMW 'modules/pulpJVM' 'PulpJVM';
  select-pane $PulpJVMW -L;
  send-to $PulpJVMW \
      '../..' Enter \
      'sbt' Enter \
      'project pulpJVM' Enter \
  ;
  select-pane $PulpJVMW -R;

  PulpJS=$TmuxSessionName:2
  new-window-splitwh-setup CatnipJS 'modules/pulpJS' 'PulpJS';
  select-pane $PulpJS -L;
  send-to $PulpJS \
      '../..' Enter \
      'sbt' Enter \
      'project pulpJS' Enter \
  ;
  select-pane $PulpJS -R;

  select-window $MainW;
}

if ! is-initiated; then
  initiate-tmux
fi

attach-tmux
