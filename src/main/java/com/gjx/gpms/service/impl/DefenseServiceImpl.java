package com.gjx.gpms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gjx.gpms.common.exception.BusinessException;
import com.gjx.gpms.dto.DefenseBatchDTO;
import com.gjx.gpms.dto.DefenseGroupDTO;
import com.gjx.gpms.dto.DefenseResultDTO;
import com.gjx.gpms.entity.*;
import com.gjx.gpms.mapper.*;
import com.gjx.gpms.security.context.UserContext;
import com.gjx.gpms.service.DefenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Defense 服务实现类。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DefenseServiceImpl implements DefenseService {

    private final DefenseBatchMapper defenseBatchMapper;
    private final DefenseGroupMapper defenseGroupMapper;
    private final DefenseGroupMemberMapper defenseGroupMemberMapper;
    private final DefenseArrangementMapper defenseArrangementMapper;
    private final DefenseResultMapper defenseResultMapper;

    /**
     * 查询列表batches相关逻辑。
     */
    @Override
    public List<DefenseBatch> listBatches(Long batchId) {
        return defenseBatchMapper.selectList(
                new LambdaQueryWrapper<DefenseBatch>()
                        .eq(batchId != null, DefenseBatch::getBatchId, batchId)
                        .orderByDesc(DefenseBatch::getCreatedAt)
        );
    }

    /**
     * 创建batch相关逻辑。
     */
    @Override
    public void createBatch(DefenseBatchDTO dto) {
        DefenseBatch entity = new DefenseBatch();
        BeanUtils.copyProperties(dto, entity);
        defenseBatchMapper.insert(entity);
        log.info("创建答辩批次：{}", dto.getName());
    }

    /**
     * 删除batch相关逻辑。
     */
    @Override
    public void deleteBatch(Long id) {
        defenseBatchMapper.deleteById(id);
    }

    /**
     * 查询列表groups相关逻辑。
     */
    @Override
    public List<DefenseGroup> listGroups(Long defenseBatchId) {
        return defenseGroupMapper.selectList(
                new LambdaQueryWrapper<DefenseGroup>()
                        .eq(DefenseGroup::getDefenseBatchId, defenseBatchId)
        );
    }

    /**
     * 创建group相关逻辑。
     */
    @Override
    @Transactional
    public void createGroup(DefenseGroupDTO dto) {
        DefenseGroup group = new DefenseGroup();
        group.setDefenseBatchId(dto.getDefenseBatchId());
        group.setName(dto.getName());
        group.setLeaderId(dto.getLeaderId());
        defenseGroupMapper.insert(group);

        if (dto.getMemberIds() != null) {
            for (Long memberId : dto.getMemberIds()) {
                DefenseGroupMember member = new DefenseGroupMember();
                member.setGroupId(group.getId());
                member.setTeacherId(memberId);
                member.setRole("member");
                defenseGroupMemberMapper.insert(member);
            }
        }

        // 组长也是成员
        DefenseGroupMember leader = new DefenseGroupMember();
        leader.setGroupId(group.getId());
        leader.setTeacherId(dto.getLeaderId());
        leader.setRole("leader");
        defenseGroupMemberMapper.insert(leader);

        log.info("创建答辩组：{}", dto.getName());
    }

    /**
     * 删除group相关逻辑。
     */
    @Override
    public void deleteGroup(Long id) {
        defenseGroupMemberMapper.delete(
                new LambdaQueryWrapper<DefenseGroupMember>().eq(DefenseGroupMember::getGroupId, id)
        );
        defenseGroupMapper.deleteById(id);
    }

    /**
     * 查询列表arrangements相关逻辑。
     */
    @Override
    public List<DefenseArrangement> listArrangements(Long groupId) {
        return defenseArrangementMapper.selectList(
                new LambdaQueryWrapper<DefenseArrangement>()
                        .eq(DefenseArrangement::getGroupId, groupId)
        );
    }

    /**
     * 新增arrangement相关逻辑。
     */
    @Override
    public void addArrangement(Long groupId, Long studentId, String defenseTime, String location) {
        DefenseGroup group = defenseGroupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException("答辩组不存在");
        }
        DefenseArrangement da = new DefenseArrangement();
        da.setGroupId(groupId);
        da.setDefenseBatchId(group.getDefenseBatchId());
        da.setStudentId(studentId);
        da.setDefenseTime(defenseTime != null ? java.time.LocalDateTime.parse(defenseTime) : null);
        da.setLocation(location);
        defenseArrangementMapper.insert(da);
    }

    /**
     * 保存result相关逻辑。
     */
    @Override
    public void saveResult(DefenseResultDTO dto) {
        DefenseResult result = defenseResultMapper.selectOne(
                new LambdaQueryWrapper<DefenseResult>()
                        .eq(DefenseResult::getArrangementId, dto.getArrangementId())
        );

        if (result == null) {
            result = new DefenseResult();
        }

        result.setArrangementId(dto.getArrangementId());
        result.setScoreItems(dto.getScoreItems());
        result.setTotalScore(dto.getTotalScore());
        result.setDecision(dto.getDecision());
        result.setComment(dto.getComment());
        result.setRecordedBy(UserContext.getUserId());

        if (result.getId() == null) {
            defenseResultMapper.insert(result);
        } else {
            defenseResultMapper.updateById(result);
        }

        log.info("保存答辩结果：安排[{}]，决策[{}]", dto.getArrangementId(), dto.getDecision());
    }

    /**
     * 获取Result。
     */
    @Override
    public DefenseResult getResult(Long arrangementId) {
        return defenseResultMapper.selectOne(
                new LambdaQueryWrapper<DefenseResult>()
                        .eq(DefenseResult::getArrangementId, arrangementId)
        );
    }
}
