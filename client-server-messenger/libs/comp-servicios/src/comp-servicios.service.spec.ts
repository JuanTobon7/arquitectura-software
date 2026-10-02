import { Test, TestingModule } from '@nestjs/testing';
import { CompServiciosService } from './comp-servicios.service.js';

describe('CompServiciosService', () => {
  let service: CompServiciosService;

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [CompServiciosService],
    }).compile();

    service = module.get<CompServiciosService>(CompServiciosService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
