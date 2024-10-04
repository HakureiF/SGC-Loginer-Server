package com.seer.seerweb.controller;

import com.seer.seerweb.annotation.AccessLimit;
import com.seer.seerweb.component.ConventionalMode;
import com.seer.seerweb.entity.vo.BagPetVO;
import com.seer.seerweb.entity.vo.VerifyBagVO;
import com.seer.seerweb.entity.vo.VerifySuitVO;
import com.seer.seerweb.service.ConventionalService;
import com.seer.seerweb.utils.ResultUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conventional")
public class ConventionalController {
    @Autowired
    ConventionalService conventionalService;
    @Autowired
    ConventionalMode conventionalMode;

    /**
     * 进入房间前：校验背包
     * @param bagVO
     * @return
     */
    @PostMapping("/verifyBag")
    public ResultUtil<String> verifyBag(@RequestBody VerifyBagVO bagVO) {
        try{
            String mess = conventionalService.verifyBag(bagVO);
            if (mess.isBlank()) {
                return ResultUtil.success();
            }
            return ResultUtil.fail(mess, null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.fail(e.getMessage(), null);
        }
    }

    /**
     * 进入房间后，刷新背包
     * @param id
     * @param vo
     * @return
     */
    @PostMapping("/freshBag")
    public ResultUtil<String> freshBag(@RequestHeader("seer-userid") String id, @RequestBody List<BagPetVO> vo) {
        try{
            String mess = conventionalService.freshBag(id, vo);
            if (mess.isBlank()) {
                return ResultUtil.success();
            }
            return ResultUtil.fail(mess, null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.fail(e.getMessage(), null);
        }
    }

    /**
     * 获取己方背包、pick、首发、被ban
     * @param id
     * @return
     */
    @GetMapping("/getPetState")
    public ResultUtil<Map<String, List<BagPetVO>>> getPetState(@RequestHeader("seer-userid") String id) {
        try{
            Map<String, List<BagPetVO>> petState = conventionalService.getPetState(id);
            return ResultUtil.success(petState);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.fail(e.getMessage(), null);
        }
    }

    @GetMapping("/getMatchScoreBoard")
    @AccessLimit(seconds = 2, maxCount = 20)
    public ResultUtil<List<Map<String, Object>>> getMatchScoreBoard(@RequestParam("groupId") String groupId) {
        try{
            List<Map<String, Object>> scoreBoard = conventionalService.getMatchScoreBoard(groupId);
            return ResultUtil.success(scoreBoard);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.fail(e.getMessage(), null);
        }
    }

    @PostMapping("/verifySuit")
    public ResultUtil<String> verifySuit(@RequestBody VerifySuitVO suitVO) {
        try{
            String mess = conventionalService.verifySuit(suitVO);
            if (mess.isBlank()) {
                return ResultUtil.success();
            }
            return ResultUtil.fail(mess, null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.fail(e.getMessage(), null);
        }
    }

    @GetMapping("/getBanNum")
    public ResultUtil<Integer> getBanNum(@RequestHeader("seer-userid") String id) {
        try{
            Integer banNum = conventionalService.getBanNum(id);
            return ResultUtil.success(banNum);
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.fail(e.getMessage(), null);
        }
    }

    @GetMapping("/getMatchPlayers")
    public ResultUtil<List<String>> getMatchPlayers() {
        try {
            return ResultUtil.success(conventionalMode.getMatchPlayers());
        } catch (Exception e) {
            e.printStackTrace();
            return ResultUtil.fail(e.getMessage(), null);
        }
    }
}
