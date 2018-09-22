package mutual

case class Gate() {
  private var _stateName: GateStateNames.Value = GateStateNames.Closed
  private var _state: Int = 0

  def stateName = _stateName
  def state = _state

  def closeByOneStep()= {
    if(_state > 0){
      _state -= 1
      _stateName = GateStateNames.InProgressClose
    }
    if(_state == 0) _stateName = GateStateNames.Closed
  }

  def openByOneStep()= {
    if(_state < 10){
      _state += 1
      _stateName = GateStateNames.InProgressOpen
    }
    if(_state == 10) _stateName = GateStateNames.Open
  }

  def stop() = {
    _stateName = GateStateNames.Stopped
  }
}
