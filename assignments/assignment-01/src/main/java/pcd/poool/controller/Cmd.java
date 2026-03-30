package pcd.poool.controller;

import pcd.poool.model.Physics;
import pcd.sketch02.model.Counter;

public interface Cmd {
	
	void execute(Physics model) throws InterruptedException;
}
