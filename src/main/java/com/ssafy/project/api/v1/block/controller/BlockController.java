package com.ssafy.project.api.v1.block.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssafy.project.api.v1.block.service.BlockService;

@RestController
@RequestMapping("api/v1/blocks")
public class BlockController {
	
	private final BlockService blockService;
	
	public BlockController(BlockService blockService) {
		this.blockService = blockService;
	}
}
