package com.gjx.gpms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gjx.gpms.common.exception.BusinessException;
import com.gjx.gpms.dto.DefenseArrangementDTO;
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

import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

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
    private final BatchMapper batchMapper;

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
        if (batchMapper.selectById(dto.getBatchId()) == null) {
            throw new BusinessException("毕设批次不存在");
        }
        DefenseBatch entity = new DefenseBatch();
        BeanUtils.copyProperties(dto, entity);
        defenseBatchMapper.insert(entity);
        log.info("创建答辩批次：{}", dto.getName());
    }

    /**
     * 删除batch相关逻辑。
     */
    @Override
    @Transactional
    public void deleteBatch(Long id) {
        DefenseBatch batch = defenseBatchMapper.selectById(id);
        if (batch == null) {
            throw new BusinessException("答辩批次不存在");
        }
        List<DefenseGroup> groups = listGroups(id);
        for (DefenseGroup group : groups) {
            deleteGroup(group.getId());
        }
        List<DefenseArrangement> arrangements = defenseArrangementMapper.selectList(
                new LambdaQueryWrapper<DefenseArrangement>().eq(DefenseArrangement::getDefenseBatchId, id)
        );
        for (DefenseArrangement arrangement : arrangements) {
            defenseResultMapper.delete(
                    new LambdaQueryWrapper<DefenseResult>()
                            .eq(DefenseResult::getArrangementId, arrangement.getId())
            );
        }
        defenseArrangementMapper.delete(
                new LambdaQueryWrapper<DefenseArrangement>().eq(DefenseArrangement::getDefenseBatchId, id)
        );
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
        if (defenseBatchMapper.selectById(dto.getDefenseBatchId()) == null) {
            throw new BusinessException("答辩批次不存在");
        }
        DefenseGroup group = new DefenseGroup();
        group.setDefenseBatchId(dto.getDefenseBatchId());
        group.setName(dto.getName());
        group.setLeaderId(dto.getLeaderId());
        defenseGroupMapper.insert(group);

        if (dto.getMemberIds() != null) {
            for (Long memberId : dto.getMemberIds()) {
                if (Objects.equals(memberId, dto.getLeaderId())) {
                    continue;
                }
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
    @Transactional
    public void deleteGroup(Long id) {
        if (defenseGroupMapper.selectById(id) == null) {
            throw new BusinessException("答辩组不存在");
        }
        defenseGroupMemberMapper.delete(
                new LambdaQueryWrapper<DefenseGroupMember>().eq(DefenseGroupMember::getGroupId, id)
        );
        List<DefenseArrangement> arrangements = defenseArrangementMapper.selectList(
                new LambdaQueryWrapper<DefenseArrangement>().eq(DefenseArrangement::getGroupId, id)
        );
        for (DefenseArrangement arrangement : arrangements) {
            defenseResultMapper.delete(
                    new LambdaQueryWrapper<DefenseResult>()
                            .eq(DefenseResult::getArrangementId, arrangement.getId())
            );
        }
        defenseArrangementMapper.delete(
                new LambdaQueryWrapper<DefenseArrangement>().eq(DefenseArrangement::getGroupId, id)
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
    public void addArrangement(DefenseArrangementDTO dto) {
        DefenseGroup group = defenseGroupMapper.selectById(dto.getGroupId());
        if (group == null) {
            throw new BusinessException("答辩组不存在");
        }
        DefenseArrangement da = new DefenseArrangement();
        da.setGroupId(dto.getGroupId());
        da.setDefenseBatchId(group.getDefenseBatchId());
        da.setStudentId(dto.getStudentId());
        try {
            da.setDefenseTime(dto.getDefenseTime() != null && !dto.getDefenseTime().isBlank()
                    ? java.time.LocalDateTime.parse(dto.getDefenseTime())
                    : null);
        } catch (DateTimeParseException e) {
            throw new BusinessException("答辩时间格式不正确");
        }
        da.setLocation(dto.getLocation());
        defenseArrangementMapper.insert(da);
    }

    /**
     * 删除arrangement相关逻辑。
     */
    @Override
    @Transactional
    public void deleteArrangement(Long id) {
        if (defenseArrangementMapper.selectById(id) == null) {
            throw new BusinessException("答辩安排不存在");
        }
        defenseResultMapper.delete(
                new LambdaQueryWrapper<DefenseResult>()
                        .eq(DefenseResult::getArrangementId, id)
        );
        defenseArrangementMapper.deleteById(id);
    }

    /**
     * 保存result相关逻辑。
     */
    @Override
    public void saveResult(DefenseResultDTO dto) {
        if (defenseArrangementMapper.selectById(dto.getArrangementId()) == null) {
            throw new BusinessException("答辩安排不存在");
        }
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
